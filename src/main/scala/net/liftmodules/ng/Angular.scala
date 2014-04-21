package net.liftmodules.ng

import scala.collection.mutable
import scala.xml.NodeSeq

import net.liftweb.http.RequestVar
import net.liftweb.common.{Failure, Full, Empty, Box}
import net.liftweb.http. { LiftRules, DispatchSnippet, ResourceServer, S }
import net.liftweb.http.js.JE._
import net.liftweb.http.js.JsCmds._
import net.liftweb.http.js.{JsExp, JsCmd, JsObj}
import net.liftweb.json.Serialization.write
import net.liftweb.json.{DefaultFormats, JsonParser}
import net.liftweb.actor.LAFuture
import net.liftweb.json.JsonAST.JString

/**
 * Dynamically generates angular modules at page render time.
 *
 * Usage:
 * {{{
 * def render = renderIfNotAlreadyDefined(
 *   angular.module("zen.lift.goals")
 *     .factory("goals", jsObjFactory()
 *       .jsonCall("getJoined", GoalsClient.getGoals())
 *       .jsonCall("join", (goalId: String) => GoalsClient.joinIndividualGoal(getUsername, getDisplayName, goalId))
 *       .jsonCall("checkIn", (checkIn: CheckIn) => GoalsClient.checkIn(checkIn.goalId, checkIn.instanceId, ...))
 *     )
 * )
 * }}}
 */
object Angular extends DispatchSnippet {
  
  /** Init function to be called in Boot */
  def init():Unit = {
    LiftRules.snippetDispatch.append {
      case "Angular" => this
    }
    LiftRules.addToPackages("net.liftmodules.ng")

    // Force loading
    liftproxyjs

    ResourceServer.allow {
      case "net" :: "liftmodules" :: "ng" :: "js" :: _ => true
    }
  }
  
  implicit val formats = DefaultFormats

  private object AngularModules extends RequestVar[mutable.HashSet[Module]](mutable.HashSet.empty)

  /**
   * Set to true when render is called so we know to stop saving things up to put in the head.
   */
  private object HeadRendered extends RequestVar[Boolean](false)
  
  /** Implementation of dispatch to allow us to add ourselves as a snippet */
  override def dispatch = { 
    case _ => { _ => render }
  }

  /**
   * Renders all the modules that have been added to the RequestVar.
   */
  def render: NodeSeq = {
    // We should only call this once from the <head> tag. Calling it again indicates a programming error.
    require(!HeadRendered.is, "render has already been called once")

    HeadRendered.set(true)
    Script(AngularModules.is.foldLeft(liftproxyjs)((jsCmd, module) => jsCmd & module.cmd))
  }
  
  private lazy val liftproxyjs:JsCmd = 
    LiftRules.loadResourceAsString("/net/liftmodules/ng/js/liftproxy.js").map(JsRaw(_):JsCmd).openOr(Noop)
  

  /**
   * Registers the module with the RequestVar so that it may be rendered in base.html.
   */
  def renderIfNotAlreadyDefined(module: Module): NodeSeq = {
    if (HeadRendered.is) {
      if (AngularModules.is.contains(module)) {
        // module already added elsewhere. normal case. don't render it again.
        NodeSeq.Empty
      } else {
        // module not rendered already in head or elsewhere. render it now, and keep it so we can deduplicate it later
        AngularModules.is += module
        Script(module.cmd)
      }
    } else {
      // New module and head render hasn't been called. Store it for head render.
      AngularModules.is += module
      NodeSeq.Empty
    }
  }

  object angular {

    def module(moduleName: String) = new Module(moduleName)
  }

  /**
   * Builder for Angular modules.
   *
   * @param dependencies other modules whose services and scopes this module depends upon.
   *                     NOTE: factories may add additional module dependencies to this as they're defined.
   */
  class Module(private[Angular] val name: String, dependencies: Set[String] = Set.empty) {

    require(name.nonEmpty)

    private val factories = Map.newBuilder[String, Factory]

    def factory(serviceName: String, factory: Factory): Module = {
      factories += serviceName -> factory
      this
    }

    private[Angular] def cmd: JsCmd = {
      val finalFactories = factories.result()
      val allDependencies: List[Str] = finalFactories
        .values
        .foldLeft(Set.newBuilder[String] ++= dependencies)(_ ++= _.moduleDependencies)
        .result()
        .map(Str)(collection.breakOut)

      val moduleDeclaration = Call("angular.module", name, JsArray(allDependencies))
      finalFactories.foldLeft(moduleDeclaration) {
        case (module, (factName, factory)) =>
          Call(JsVar(module.toJsCmd, "factory").toJsCmd, factName, factory.toGenerator)
      }
    }

    override def hashCode(): Int = name.hashCode

    override def equals(obj: Any): Boolean =
      obj != null && obj.isInstanceOf[Module] && {
        val otherModule = obj.asInstanceOf[Module]
        otherModule.name == name
      }
  }

  /**
   * A factory builder that can create a javascript object full of ajax calls.
   */
  def jsObjFactory() = new JsObjFactory()

  /**
   * Creates a generator function() {} to be used within an angular.module.factory(name, ...) call.
   */
  trait Factory {

    private[Angular] def moduleDependencies: Set[String] = Set.empty[String]

    private[Angular] def toGenerator: AnonFunc
  }

  /**
   * Produces a javascript object with ajax functions as keys. e.g.
   * {{{
   * function(dependencies) {
   *   get: function() { doAjaxStuff(); }
   *   post: function(string) { doAjaxStuff(); }
   * }
   * }}}
   */
  class JsObjFactory extends Factory {

    /**
     * name -> function
     */
    private val functions = mutable.HashMap.empty[String, AjaxFunctionGenerator]

    override private[Angular] def moduleDependencies =
      functions.values.foldLeft(Set.newBuilder[String])(_ ++= _.moduleDependencies).result()

    private val promiseMapper = DefaultApiSuccessMapper

    /**
     * Registers a javascript function in this service's javascript object that takes a String and returns a \$q promise.
     *
     * @param functionName name of the function to be made available on the service/factory
     * @param func produces the result of the ajax call. Failure, Full(DefaultResponse(false)), and some other logical
     *             failures will be mapped to promise.reject(). See promiseMapper.
     */
    def jsonCall(functionName: String, func: String => Box[AnyRef]): JsObjFactory = {
      registerFunction(functionName, AjaxStringToJsonFunctionGenerator(func.andThen(promiseMapper.toPromise)))
    }

    /**
     * Registers a javascript function in this service's javascript object that takes an NgModel object and returns a
     * \$q promise.
     *
     * @param functionName name of the function to be made available on the service/factory
     * @param func produces the result of the ajax call. Failure, Full(DefaultResponse(false)), and some other logical
     *             failures will be mapped to promise.reject(). See promiseMapper.
     */
    def jsonCall[Model <: NgModel : Manifest](functionName: String, func: Model => Box[Any]): JsObjFactory = {
      registerFunction(functionName, AjaxJsonToJsonFunctionGenerator(func.andThen(promiseMapper.toPromise)))
    }

    /**
     * Registers a no-arg javascript function in this service's javascript object that returns a \$q promise.
     *
     * @param functionName name of the function to be made available on the service/factory
     * @param func produces the result of the ajax call. Failure, Full(DefaultResponse(false)), and some other logical
     *             failures will be mapped to promise.reject(). See promiseMapper.
     */
    def jsonCall(functionName: String, func: => Box[AnyRef]): JsObjFactory = {
      registerFunction(functionName, AjaxNoArgToJsonFunctionGenerator(() => promiseMapper.toPromise(func)))
    }

    def future[T <: Any](functionName: String, func: => LAFuture[Box[T]]): JsObjFactory = {
      registerFunction(functionName, FutureFunctionGenerator(() => promiseMapper.toFuturePromise(func)))
    }

    def jsonFuture[Model <: NgModel : Manifest, T <: Any](functionName: String, func: Model => LAFuture[T]): JsObjFactory = {
      registerFunction(functionName, null)
    }

    /**
     * Registers a no-arg javascript function in this service's javascript object that returns a String value.
     * Use this to provide string values which are known at page load time and do not change.
     *
     * @param functionName name of the function to be made available on the service/factory
     * @param value value to be returned on invocation of this function in the client.
     */
    def string(functionName: String, value:String): JsObjFactory =
      registerFunction(functionName, ToStringFunctionGenerator(value))

    /**
     * Registers a no-arg javascript function in this service's javascript object that returns an AnyVal value.
     * Use this to provide primitive values which are known at page load time and do not change.
     *
     * @param functionName name of the function to be made available on the service/factory
     * @param value value to be returned on invocation of this function in the client.
     */
    def anyVal(functionName: String, value:AnyVal): JsObjFactory =
      string(functionName, value.toString)

    /**
     * Registers a no-arg javascript function in this service's javascript object that returns a json object.
     * Use this to provide objects which are known at page load time and do not change.
     *
     * @param functionName name of the function to be made available on the service/factory
     * @param value value to be returned on invocation of this function in the client.
     */
    def json(functionName: String, value:AnyRef): JsObjFactory =
      registerFunction(functionName, ToJsonFunctionGenerator(value))

    /**
     * Adds the ajax function factory and its dependencies to the factory.
     */
    private def registerFunction(functionName: String, generator: AjaxFunctionGenerator): JsObjFactory = {
      require(functionName.nonEmpty)
      functions += functionName -> generator
      this
    }

    private[Angular] def toGenerator: AnonFunc = {
      val serviceDependencies = functions.values.foldLeft(Set.newBuilder[String])(_ ++= _.serviceDependencies).result()
      AnonFunc(serviceDependencies.mkString(","), JsReturn(JsObj(functions.mapValues(_.toAnonFunc).toSeq: _*)))
    }

    /**
     * Maps an api result to a Promise object that will be used to fulfill the javascript promise object.
     */
    object DefaultApiSuccessMapper extends PromiseMapper {

      def toPromise(box: Box[Any]): Promise = {
        box match {
          case Full(jsExp: JsExp) => Resolve(Some(jsExp)) // prefer using a case class instead
          case Full(serializable: AnyRef) => Resolve(Some(JsRaw(write(serializable))))
          case Full(other) => Resolve(Some(JsRaw(other.toString)))
          case Full(Unit) | Empty => Resolve()
          case Failure(msg, _, _) => Reject(msg)
        }
      }

      def toFuturePromise[T <: Any](future: LAFuture[Box[T]]) = {
        val fp = new LAFuture[Promise]
        future.foreach(v => fp.satisfy(toPromise(v)))
        fp
      }
    }

  }

  /**
   * Maps the response passed into the ajax calls into something that can be passed into promise.resolve(data) or
   * promise.reject(reason).
   */
  trait PromiseMapper {

    def toPromise(box: Box[Any]): Promise
    def toFuturePromise[T <: Any](future: LAFuture[Box[T]]): LAFuture[Promise]
  }

  /**
   * Used to resolve or reject a javascript angular \$q promise.
   */
  sealed trait Promise

  case class Resolve(data: Option[JsExp] = None) extends Promise

  case class Reject(reason: String = "server error") extends Promise

  object Promise {

    def apply(success: Boolean): Promise = if (success) Resolve(None) else Reject()
  }

  protected case class AjaxNoArgToJsonFunctionGenerator(jsFunc: () => Promise) extends LiftAjaxFunctionGenerator {

    def toAnonFunc = AnonFunc(JsReturn(Call("liftProxy", liftPostData)))

    private def liftPostData = SHtmlExtensions.ajaxJsonPost((id) => promiseToJson(jsFunc()))
  }

  protected case class AjaxStringToJsonFunctionGenerator(stringToPromise: (String) => Promise)
    extends LiftAjaxFunctionGenerator {

    private val ParamName = "str"

    def toAnonFunc = AnonFunc(ParamName, JsReturn(Call("liftProxy", liftPostData)))

    private def liftPostData = SHtmlExtensions.ajaxJsonPost(JsVar(ParamName), jsonFunc)

    private def jsonFunc: String => JsObj = {
      val jsonToPromise = (json: String) => JsonParser.parse(json).extractOpt[RequestString] match {
        case Some(RequestString(id, data)) => stringToPromise(data)
        case None => Reject("invalid json")
      }
      jsonToPromise andThen promiseToJson
    }
  }

  protected case class AjaxJsonToJsonFunctionGenerator[Model <: NgModel : Manifest](modelToPromise: Model => Promise)
    extends LiftAjaxFunctionGenerator {

    private val ParamName = "json"

    def toAnonFunc = AnonFunc(ParamName, JsReturn(Call("liftProxy", liftPostData)))

    private def liftPostData: JsExp = SHtmlExtensions.ajaxJsonPost(JsVar(ParamName), jsonFunc)

    private def jsonFunc: String => JsObj = {
      val jsonToPromise = (json: String) => JsonParser.parse(json).\\("data").extractOpt[Model] match {
        case Some(model) => modelToPromise(model)
        case None => Reject("invalid json")
      }
      jsonToPromise andThen promiseToJson
    }
  }

  protected case class FutureFunctionGenerator(func: () => LAFuture[Promise]) extends LiftAjaxFunctionGenerator {
    def toAnonFunc = AnonFunc(JsReturn(Call("liftProxy", liftPostData)))

    private def liftPostData = SHtmlExtensions.ajaxJsonPost(jsonFunc)

    private def jsonFunc: String => JsObj = {

      val jsonToFuture:(String) => LAFuture[Promise] = json => JsonParser.parse(json) \\ "id" match {
        case JString(id) => callFuture(id)
        case _ => reject
      }

      val futureToJsObj = (f:LAFuture[Promise]) =>
        if(f.isSatisfied)
          promiseToJson(f.get)
        else
          JsObj("future" -> JsTrue)

      jsonToFuture andThen futureToJsObj
    }

    private def callFuture(id:String) = {
      val f = func()
      println("Received call "+id)
      S.session map { s =>
        f.foreach{ p =>
          println("Future resolved to" + p)
          s.sendCometActorMessage("LiftNgFutureActor", Empty, ReturnData(id, p))
        }
      }
      f
    }

    private val reject = {
      val f = new LAFuture[Promise]
      f.satisfy(Reject("invalid json"))
      f
    }
  }

//  protected case class AjaxFutureJsonToJsonFunctionGenerator[Model <: NgModel : Manifest]
//    extends LiftAjaxFunctionGenerator {
//
//  }

  protected case class ToStringFunctionGenerator(s:String) extends LiftAjaxFunctionGenerator {
    def toAnonFunc = AnonFunc(JsReturn(s))
  }

  protected case class ToJsonFunctionGenerator(obj:AnyRef) extends LiftAjaxFunctionGenerator {
    def toAnonFunc = AnonFunc(JsReturn(JsRaw(write(obj))))
  }

  trait AjaxFunctionGenerator {

    def moduleDependencies: Set[String]

    def serviceDependencies: Set[String]

    def toAnonFunc: AnonFunc
  }

  trait LiftAjaxFunctionGenerator extends AjaxFunctionGenerator {

    def moduleDependencies: Set[String] = Set("zen.lift.proxy")

    def serviceDependencies: Set[String] = Set("liftProxy")

    private val SuccessField = "success"

    protected def promiseToJson(promise: Promise): JsObj = {
      promise match {
        case Resolve(Some(jsExp)) => JsObj(SuccessField -> JsTrue, "data" -> jsExp)
        case Resolve(None) => JsObj(SuccessField -> JsTrue)
        case Reject(reason) => JsObj(SuccessField -> JsFalse, "msg" -> reason)
      }
    }
  }

  /**
   * A model to be sent from angularjs as json, to lift deserialized into this class.
   */
  trait NgModel

  case class RequestData[Model <: NgModel : Manifest](id:String, data:Model)
  case class RequestString(id:String, data:String)
  case class ReturnData(id:String, data:Any)
}

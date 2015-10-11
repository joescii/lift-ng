package net.liftmodules.ng

import java.util.concurrent.{ ConcurrentMap, ConcurrentHashMap }

import scala.collection.mutable
import scala.xml.{Elem, NodeSeq}

import net.liftweb.actor.LAFuture
import net.liftweb.common._
import net.liftweb.json.{DefaultFormats, Formats, JsonParser}
import net.liftweb.http. { LiftRules, DispatchSnippet, ResourceServer, S, RequestVar, SessionVar }
import net.liftweb.http.js.JE._
import net.liftweb.http.js.JsCmds._
import net.liftweb.http.js.{JsExp, JsCmd, JsObj}
import net.liftweb.util.Props
import net.liftweb.util.Props.RunModes
import net.liftweb.util.StringHelpers._

/**
 * Primary lift-ng module
 */
object Angular extends DispatchSnippet with AngularProperties with LiftNgJsHelpers with Loggable {

  private [ng] var futuresDefault:Boolean = true
  private [ng] var appSelectorDefault:String = "[ng-app]"
  private [ng] var includeJsScript:Boolean = true
  private [ng] def rand = "NG"+randomString(18)

  /**
    * Init function to be called in Boot
    * @param futures true to include {{{net.liftweb.actor.LAFuture}}} support (and hence add a comet to your page), false otherwise
    * @param appSelector the CSS selector to find your app in the page
    * @param includeJsScript true to include the prerequisite liftproxy.js file, false if you plan to include it yourself.
    * @param includeAngularJs TODO
    * @param additionalAngularJsModules TODO
    */
  def init(futures:Boolean = true,
           appSelector:String = "[ng-app]",
           includeJsScript:Boolean = true,
           includeAngularJs:Boolean = false,
           additionalAngularJsModules:List[String] = List()
           ):Unit = {
    LiftRules.snippetDispatch.append {
      case "Angular" => this
      case "i18n" => AngularI18n
    }
    LiftRules.addToPackages("net.liftmodules.ng")

    ResourceServer.allow {
      case "net" :: "liftmodules" :: "ng" :: "js" :: _ => true
    }

    futuresDefault = futures
    appSelectorDefault = appSelector
    this.includeJsScript = includeJsScript

    AngularI18nRest.init()

    if(includeAngularJs) AngularJsRest.init()
  }

  private def bool(s:String, default:Boolean):Boolean = {
    val truthy = List("true", "yes", "on")
    val falsey = List("false", "no", "off")

    if(default) !falsey.find(_.equalsIgnoreCase(s)).isDefined
    else truthy.find(_.equalsIgnoreCase(s)).isDefined
  }
  
  private object AngularModules extends RequestVar[mutable.HashSet[Module]](mutable.HashSet.empty)

  /**
   * Set to true when render is called so we know to stop saving things up to put in the head.
   */
  private object HeadRendered extends RequestVar[Boolean](false)
  
  /** Implementation of dispatch to allow us to add ourselves as a snippet */
  override def dispatch = {
    case "bind" => bind
    case _ => { _ => render }
  }

  private val liftproxySrc =
    "/classpath/net/liftmodules/ng/js/liftproxy-"+BuildInfo.version + (Props.mode match {
      case RunModes.Development => ".js"
      case _ => ".min.js"
    })

  private def classFromName(name:String):Option[Class[_]] = {
    try Some(Class.forName(name)) catch {
      case e:Exception => None
    }
  }
  /**
   * Sets up a two-way scope variable binding by dropping in two binding actors as the last Elem/Node children
   * in the passed NodeSeq
   * @param xhtml
   * @return
   */
  def bind(xhtml:NodeSeq):NodeSeq = {
    val t = S.attr("type").map(_.trim)
    val ts = S.attr("types").map(_.split(',').map(_.trim).toList).openOr(Seq())
    val types = t.map(_ +: ts).openOr(ts)

    def cometClass(name:String) = LiftRules.buildPackage("comet."+name)
      .map(clazz => classFromName(clazz))
      .find(_.isDefined)
      .map(_.get)

    def isToClient(clazz:Class[_])      = classOf[BindingToClient] isAssignableFrom clazz
    def isToServer(clazz:Class[_])      = classOf[BindingToServer] isAssignableFrom clazz
    def isSessionScoped(clazz:Class[_]) = classOf[SessionScope] isAssignableFrom clazz

    val divs = types.map { bType =>
      val clazz = cometClass(bType)

      def cometUnnamed = {
        val cometUnnamed = "comet?type=" + bType
        <div data-lift={cometUnnamed}></div>
      }

      def cometNamed = {
        val cometNamed = "comet?type=" + bType + "&randomname=true"
        <div data-lift={cometNamed}></div>
      }

      def ajax(theClass:Class[_], inSession:Boolean) = {
        def newBinder = theClass.newInstance.asInstanceOf[NgModelBinder[NgModel]]

        val binder =
          if(inSession)
            getToServerBinder(bType).openOr(addToServerBinder(bType, newBinder))
          else
            newBinder

        binder.fixedRender.openOrThrowException("lift-ng has a bug in it. Please report it at https://github.com/joescii/lift-ng")
      }

      clazz.map { c =>
        val toClient = isToClient(c)
        val toServer = isToServer(c)
        val session  = isSessionScoped(c)

        if(session) {
          // We need to render the named comet first.  Otherwise using CometListener does not work.
          // This is because the unnamed comet sends the messages up via the named comet.
          // Hence it will get a create message but have no named comet actor to use.
          if (toClient && toServer) cometNamed ++ cometUnnamed
          else if (toClient) cometUnnamed
          else ajax(c, session)
        }
        else {
          if(toClient) cometNamed
          else ajax(c, session)
        }

      }.getOrElse(NodeSeq.Empty)

    }.reduceOption(_ ++ _)

    divs match {
      case Some(ds) => xhtml.toList match {
        case (n:Elem) :: tail => n.copy(child = n.child ++ ds) :: tail
        case _ => xhtml // I don't think this case can actually happen...
      }

      case None => {
        logger.warn("Angular.bind utilized without 'type' or 'types' specified")
        xhtml
      }
    }
  }

  private object ToServerBinders extends SessionVar[ConcurrentMap[String, NgModelBinder[NgModel]]](new ConcurrentHashMap())
  private def addToServerBinder(theType:String, b:NgModelBinder[NgModel]):NgModelBinder[NgModel] = {
    ToServerBinders.get.put(theType, b)
    b
  }
  def getToServerBinder(theType:String):Box[NgModelBinder[NgModel]] = Option(ToServerBinders.get.get(theType))

  /**
   * Renders all the modules that have been added to the RequestVar.
   */
  def render: NodeSeq = {
    // We should only call this once from the <head> tag. Calling it again indicates a programming error.
    require(!HeadRendered.is, "render has already been called once")

    HeadRendered.set(true)

    val includeFutures = S.attr("futures").map(bool(_, futuresDefault)).openOr(futuresDefault)

    val liftproxy = if(includeJsScript) <script src={liftproxySrc}></script> else NodeSeq.Empty
    val jsModule = Script(JsRaw(
    //net_liftmodules_ng.contextPath + '/ajax_request/' + lift_page + '/'
      "var net_liftmodules_ng=net_liftmodules_ng||{};" +
      "net_liftmodules_ng.endpoint=function(){return "+ajaxEndpoint.get+";};" +
      "net_liftmodules_ng.version=\"" + BuildInfo.version + "\";" +
      "net_liftmodules_ng.jsPath=\"" + liftproxySrc +"\";"
    ))
    val modules = Script(AngularModules.is.map(_.cmd).reduceOption(_ & _).getOrElse(Noop))
    val futureActor = if(includeFutures) <div data-lift="comet?type=LiftNgFutureActor"></div> else NodeSeq.Empty

    liftproxy ++ jsModule ++ modules ++ futureActor
  }

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

  private [ng] def plumbFuture[T <: Any](f: LAFuture[Box[T]], id:String)(implicit formats:Formats) = {
    S.session map { s => f foreach { box =>
      val json = S.initIfUninitted(s)(box map stringify)
      s.sendCometActorMessage("LiftNgFutureActor", Empty, ReturnData(id, json))
    }}
    f
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
   * This exists to wrap the functions passed to `defModelToAny`/`jsonCall` and handle the type contravariance
   * appropriately.  Users of lift-ng needn't even know it exists.
   * See http://stackoverflow.com/questions/31907701/why-does-this-scala-function-compile-when-the-argument-does-not-conform-to-the-t/31907828#answer-31907813
   */
  case class ModelFnBox[M](f: M => Box[Any])

  /**
   * This exists to wrap the functions passed to `defModelToFutureAny`/`future` and handle the type contravariance
   * appropriately.  Users of lift-ng needn't even know it exists.
   * See http://stackoverflow.com/questions/31907701/why-does-this-scala-function-compile-when-the-argument-does-not-conform-to-the-t/31907828#answer-31907813
   */
  case class ModelFnFuture[M, R](f: M => LAFuture[Box[R]])

  /**
   * Transparently wraps `NgModel => Box[Any]` functions appropriately for `defModelToAny`/`jsonCall`
   */
  implicit def wrapModelFnBox[M](f: M => Box[Any]) = ModelFnBox(f)

  /**
   * Transparently wraps `NgModel => LAFuture[Box[Any]]` functions appropriately for `defModelToFutureAny`
   */
  implicit def wrapModelFnFuture[M, R](f: M => LAFuture[Box[R]]) = ModelFnFuture(f)

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
     * Registers a no-arg javascript function in this service's javascript object that returns a \$q promise.
     * This function is an enhancement over `jsonCall` in that it allows the caller to provide an implicit `Formats`
     * for dictating the JSON serializer.
     * <p>
     * Future plan is to include a macro named `defs` which will choose the appropriate def*() function based on
     * the type of the argument.
     *
     * @param functionName name of the function to be made available on the service/factory
     * @param func produces the result of the ajax call. Failure, Full(DefaultResponse(false)), and some other logical
     *             failures will be mapped to promise.reject(). See promiseMapper.
     */
    def defAny
      (functionName: String, func: => Box[Any])
      (implicit formats:Formats = DefaultFormats)
      : JsObjFactory =
      registerFunction(functionName, AjaxNoArgToJsonFunctionGenerator(() => promiseMapper.toPromise(func)))


    /**
     * Registers a no-arg javascript function in this service's javascript object that returns a \$q promise.
     *
     * @param functionName name of the function to be made available on the service/factory
     * @param func produces the result of the ajax call. Failure, Full(DefaultResponse(false)), and some other logical
     *             failures will be mapped to promise.reject(). See promiseMapper.
     */
//    @deprecated(message = "", since = "0.7.0")
    def jsonCall
    (functionName: String, func: => Box[AnyRef])
    : JsObjFactory = defAny(functionName, func)

    /**
     * Registers a javascript function in this service's javascript object that takes a String and returns a \$q promise.
     * This function is an enhancement over `jsonCall` in that it allows the caller to provide an implicit `Formats`
     * for dictating the JSON serializer.
     * <p>
     * Future plan is to include a macro named `defs` which will choose the appropriate def*() function based on
     * the type of the argument.
     *
     * @param functionName name of the function to be made available on the service/factory
     * @param func produces the result of the ajax call. Failure, Full(DefaultResponse(false)), and some other logical
     *             failures will be mapped to promise.reject(). See promiseMapper.
     */
    def defStringToAny
      (functionName: String, func: String => Box[Any])
      (implicit formats:Formats = DefaultFormats)
      : JsObjFactory =
      registerFunction(functionName, AjaxStringToJsonFunctionGenerator(func.andThen(promiseMapper.toPromise)))


    /**
     * Registers a javascript function in this service's javascript object that takes a String and returns a \$q promise.
     *
     * @param functionName name of the function to be made available on the service/factory
     * @param func produces the result of the ajax call. Failure, Full(DefaultResponse(false)), and some other logical
     *             failures will be mapped to promise.reject(). See promiseMapper.
     */
//    @deprecated(message = "", since = "0.7.0")
    def jsonCall
      (functionName: String, func: String => Box[AnyRef])
      : JsObjFactory = defStringToAny(functionName, func)

    /**
     * Registers a javascript function in this service's javascript object that takes an NgModel object and returns a
     * \$q promise.
     * This function is an enhancement over `jsonCall` in that it allows the caller to provide an implicit `Formats`
     * for dictating the JSON serializer.
     * <p>
     * Future plan is to include a macro named `defs` which will choose the appropriate def*() function based on
     * the type of the argument.
     *
     * @param functionName name of the function to be made available on the service/factory
     * @param func produces the result of the ajax call. Failure, Full(DefaultResponse(false)), and some other logical
     *             failures will be mapped to promise.reject(). See promiseMapper.
     */
    def defModelToAny[Model <: NgModel]
      (functionName: String, func: ModelFnBox[Model])
      (implicit mf:Manifest[Model], formats:Formats = DefaultFormats)
      : JsObjFactory =
      registerFunction(functionName, AjaxJsonToJsonFunctionGenerator(func.f.andThen(promiseMapper.toPromise)))


    /**
     * Registers a javascript function in this service's javascript object that takes an NgModel object and returns a
     * \$q promise.
     *
     * @param functionName name of the function to be made available on the service/factory
     * @param func produces the result of the ajax call. Failure, Full(DefaultResponse(false)), and some other logical
     *             failures will be mapped to promise.reject(). See promiseMapper.
     */
//    @deprecated(message = "", since = "0.7.0")
    def jsonCall[Model <: NgModel]
      (functionName: String, func: ModelFnBox[Model])
      (implicit mf:Manifest[Model])
      : JsObjFactory = defModelToAny(functionName, func)

    /**
     * Registers a no-arg javascript function in this service's javascript object that returns a \$q promise.
     * This function is an enhancement over `future` in that it allows the caller to provide an implicit `Formats`
     * for dictating the JSON serializer.
     * <p>
     * Future plan is to include a macro named `defs` which will choose the appropriate def*() function based on
     * the type of the argument.
     *
     * @param functionName name of the function to be made available on the service/factory
     * @param func produces the result of the ajax call. Failure, Full(DefaultResponse(false)), and some other logical
     *             failures will be mapped to promise.reject(). See promiseMapper.
     */
    def defFutureAny[T <: Any]
      (functionName: String, func: => LAFuture[Box[T]])
      (implicit formats:Formats = DefaultFormats)
      : JsObjFactory =
      registerFunction(functionName, NoArgFutureFunctionGenerator(() => func))

    /**
     * Registers a no-arg javascript function in this service's javascript object that returns a \$q promise.
     *
     * @param functionName name of the function to be made available on the service/factory
     * @param func produces the result of the ajax call. Failure, Full(DefaultResponse(false)), and some other logical
     *             failures will be mapped to promise.reject(). See promiseMapper.
     */
//    @deprecated(message = "", since = "0.7.0")
    def future[T <: Any]
      (functionName: String, func: => LAFuture[Box[T]])
      : JsObjFactory =
      defFutureAny(functionName, func)

    /**
     * Registers a javascript function in this service's javascript object that takes a String and returns a \$q promise.
     * This function is an enhancement over `future` in that it allows the caller to provide an implicit `Formats`
     * for dictating the JSON serializer.
     * <p>
     * Future plan is to include a macro named `defs` which will choose the appropriate def*() function based on
     * the type of the argument.
     *
     * @param functionName name of the function to be made available on the service/factory
     * @param func produces the result of the ajax call. Failure, Full(DefaultResponse(false)), and some other logical
     *             failures will be mapped to promise.reject(). See promiseMapper.
     */
    def defStringToFutureAny[T <: Any]
      (functionName: String, func: String => LAFuture[Box[T]])
      (implicit formats:Formats = DefaultFormats)
      : JsObjFactory =
      registerFunction(functionName, StringFutureFunctionGenerator(func))

    /**
     * Registers a javascript function in this service's javascript object that takes a String and returns a \$q promise.
     *
     * @param functionName name of the function to be made available on the service/factory
     * @param func produces the result of the ajax call. Failure, Full(DefaultResponse(false)), and some other logical
     *             failures will be mapped to promise.reject(). See promiseMapper.
     */
//    @deprecated(message = "", since = "0.7.0")
    def future[T <: Any]
      (functionName: String, func: String => LAFuture[Box[T]])
      : JsObjFactory =
      defStringToFutureAny(functionName, func)

    /**
     * Registers a javascript function in this service's javascript object that takes an NgModel object and returns a
     * \$q promise.
     * This function is an enhancement over `future` in that it allows the caller to provide an implicit `Formats`
     * for dictating the JSON serializer.
     * <p>
     * Future plan is to include a macro named `defs` which will choose the appropriate def*() function based on
     * the type of the argument.
     *
     * @param functionName name of the function to be made available on the service/factory
     * @param func produces the result of the ajax call. Failure, Full(DefaultResponse(false)), and some other logical
     *             failures will be mapped to promise.reject(). See promiseMapper.
     */
    def defModelToFutureAny[Model <: NgModel, T <: Any]
      (functionName: String, func: ModelFnFuture[Model, T])
      (implicit mf:Manifest[Model], formats:Formats = DefaultFormats)
      : JsObjFactory =
      registerFunction(functionName, JsonFutureFunctionGenerator(func.f))

    /**
     * Registers a javascript function in this service's javascript object that takes an NgModel object and returns a
     * \$q promise.
     *
     * @param functionName name of the function to be made available on the service/factory
     * @param func produces the result of the ajax call. Failure, Full(DefaultResponse(false)), and some other logical
     *             failures will be mapped to promise.reject(). See promiseMapper.
     */
//    @deprecated(message = "", since = "0.7.0")
    def future[Model <: NgModel, T <: Any]
      (functionName: String, func: Model => LAFuture[Box[T]])
      (implicit mf:Manifest[Model])
      : JsObjFactory =
      defModelToFutureAny(functionName, func)

    // TODO: Rather than functions, let's put the values directly on the factory
//    /**
//     * Registers a no-arg javascript function in this service's javascript object that returns a String value.
//     * Use this to provide string values which are known at page load time and do not change.
//     *
//     * @param functionName name of the function to be made available on the service/factory
//     * @param value value to be returned on invocation of this function in the client.
//     */
//    def valAny
//      (functionName: String, value:Any)
//      (implicit formats:Formats = DefaultFormats)
//      : JsObjFactory =
//      registerFunction(functionName, FromAnyFunctionGenerator(value))

    /**
     * Registers a no-arg javascript function in this service's javascript object that returns a String value.
     * Use this to provide string values which are known at page load time and do not change.
     *
     * @param functionName name of the function to be made available on the service/factory
     * @param value value to be returned on invocation of this function in the client.
     */
//    @deprecated(message = "", since = "0.7.0")
    def string
      (functionName: String, value:String)
      (implicit formats:Formats = DefaultFormats)
      : JsObjFactory =
      registerFunction(functionName, FromAnyFunctionGenerator(value))

    /**
     * Registers a no-arg javascript function in this service's javascript object that returns an AnyVal value.
     * Use this to provide primitive values which are known at page load time and do not change.
     *
     * @param functionName name of the function to be made available on the service/factory
     * @param value value to be returned on invocation of this function in the client.
     */
//    @deprecated(message = "", since = "0.7.0")
    def anyVal
      (functionName: String, value:AnyVal)
      (implicit formats:Formats = DefaultFormats)
      : JsObjFactory =
      registerFunction(functionName, FromAnyFunctionGenerator(value))

    /**
     * Registers a no-arg javascript function in this service's javascript object that returns a json object.
     * Use this to provide objects which are known at page load time and do not change.
     *
     * @param functionName name of the function to be made available on the service/factory
     * @param value value to be returned on invocation of this function in the client.
     */
//    @deprecated(message = "", since = "0.7.0")
    def json
      (functionName: String, value:AnyRef)
      (implicit formats:Formats = DefaultFormats)
      : JsObjFactory =
      registerFunction(functionName, FromAnyFunctionGenerator(value))

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

  }

  /**
   * Maps an api result to a Promise object that will be used to fulfill the javascript promise object.
   */
  object DefaultApiSuccessMapper extends PromiseMapper {
//    private implicit val formats = DefaultFormats + new LAFutureSerializer

    def toPromise(box: Box[Any])(implicit formats:Formats): Promise = {
      box match {
        case Full(jsExp: JsExp) => Resolve(Some(jsExp)) // prefer using a case class instead
        case Full(serializable: AnyRef) => Resolve(Some(JsRaw(stringify(serializable))))
        case Full(other) => Resolve(Some(JsRaw(other.toString)))
        case Full(Unit) | Empty => Resolve()
        case Failure(msg, _, _) => Reject(msg)
      }
    }
  }

  /**
   * Maps the response passed into the ajax calls into something that can be passed into promise.resolve(data) or
   * promise.reject(reason).
   */
  trait PromiseMapper {

    def toPromise(box: Box[Any])(implicit formats:Formats): Promise
  }

  /**
   * Used to resolve or reject a javascript angular \$q promise.
   */
  sealed trait Promise

  case class Resolve(data: Option[JsExp] = None, futureId: Option[String] = None) extends Promise

  case class Reject(reason: String = "server error") extends Promise

  object Promise {

    def apply(success: Boolean): Promise = if (success) Resolve(None) else Reject()
  }

  protected case class AjaxNoArgToJsonFunctionGenerator(jsFunc: () => Promise) extends LiftAjaxFunctionGenerator {

    def toAnonFunc = AnonFunc(JsReturn(Call("liftProxy.request", liftPostData)))

    private def liftPostData = SHtmlExtensions.ajaxJsonPost((id) => promiseToJson(jsFunc()))
  }

  protected case class AjaxStringToJsonFunctionGenerator(stringToPromise: (String) => Promise)(implicit formats:Formats)
    extends LiftAjaxFunctionGenerator {

    private val ParamName = "str"

    def toAnonFunc = AnonFunc(ParamName, JsReturn(Call("liftProxy.request", liftPostData)))

    private def liftPostData = SHtmlExtensions.ajaxJsonPost(JsVar(ParamName), jsonFunc)

    private def jsonFunc: String => JsObj = {
      val jsonToPromise = (json: String) => JsonParser.parse(json).extractOpt[RequestString] match {
        case Some(RequestString(data)) => stringToPromise(data)
        case None => Reject(invalidJson(json))
      }
      jsonToPromise andThen promiseToJson
    }
  }

  protected case class AjaxJsonToJsonFunctionGenerator[Model <: NgModel](modelToPromise: Model => Promise)(implicit mf:Manifest[Model], formats:Formats)
    extends LiftAjaxFunctionGenerator {
    private val ParamName = "json"

    def toAnonFunc = AnonFunc(ParamName, JsReturn(Call("liftProxy.request", liftPostData)))

    private def liftPostData: JsExp = SHtmlExtensions.ajaxJsonPost(JsVar(ParamName), jsonFunc)

    private def jsonFunc: String => JsObj = {
      val jsonToPromise = (json: String) => JsonParser.parse(json).\\("data").extractOpt[Model] match {
        case Some(model) => modelToPromise(model)
        case None => Reject(invalidJson(json))
      }
      jsonToPromise andThen promiseToJson
    }
  }

  protected abstract class FutureFunctionGenerator extends LiftAjaxFunctionGenerator {
    protected def jsonFunc[T <: Any](jsonToFuture: (String) => NgFuture[T])(implicit formats:Formats): String => JsObj = {
      val futureToJsObj = (f:NgFuture[T]) =>
        if(f._1.isSatisfied)
          promiseToJson(DefaultApiSuccessMapper.toPromise(f._1.get))
        else
          promiseToJson(Resolve(None, Some(f._2)))

      jsonToFuture andThen futureToJsObj
    }

    protected def reject[T <: Any](json:String):NgFuture[T] = {
      val f = new LAFuture[Box[T]]
      f.satisfy(Failure(invalidJson(json)))
      (f, FutureIdNA)
    }
  }

  protected case class NoArgFutureFunctionGenerator[T <: Any](func: () => LAFuture[Box[T]])(implicit formats:Formats) extends FutureFunctionGenerator {
    def toAnonFunc = AnonFunc(JsReturn(Call("liftProxy.request", liftPostData)))

    private def liftPostData = SHtmlExtensions.ajaxJsonPost(jsonFunc(jsonToFuture))

    val jsonToFuture:(String) => NgFuture[T] = json => {
      val id = rand
      (Angular.plumbFuture(func(), id), id)
    }
  }

  protected case class StringFutureFunctionGenerator[T <: Any](func: String => LAFuture[Box[T]])(implicit formats:Formats) extends FutureFunctionGenerator {
    private val ParamName = "str"

    def toAnonFunc = AnonFunc(ParamName, JsReturn(Call("liftProxy.request", liftPostData)))

    private def liftPostData = SHtmlExtensions.ajaxJsonPost(JsVar(ParamName), jsonFunc(jsonToFuture))

    def jsonToFuture:(String) => NgFuture[T] = json => JsonParser.parse(json).extractOpt[RequestString] match {
      case Some(RequestString(data)) => {
        val id = rand
        (Angular.plumbFuture(func(data), id), id)
      }
      case _ => reject[T](json)
    }
  }

  protected case class JsonFutureFunctionGenerator[Model <: NgModel, T <: Any](func: Model => LAFuture[Box[T]])(implicit mf:Manifest[Model], formats:Formats) extends FutureFunctionGenerator {
    private val ParamName = "json"

    def toAnonFunc = AnonFunc(ParamName, JsReturn(Call("liftProxy.request", liftPostData)))

    private def liftPostData = SHtmlExtensions.ajaxJsonPost(JsVar(ParamName), jsonFunc(jsonToFuture))

    def jsonToFuture:(String) => NgFuture[T] = json => {
      val parsed = JsonParser.parse(json)
      val dataOpt = (parsed \\ "data").extractOpt[Model]
      val id = rand

      val fOpt = for {
        data <- dataOpt
      } yield {
        (Angular.plumbFuture(func(data), id), id)
      }

      fOpt.openOr(reject[T](json))
    }
  }

  protected case class ToStringFunctionGenerator(s:String)(implicit formats:Formats) extends LiftAjaxFunctionGenerator {
    def toAnonFunc = AnonFunc(JsReturn(s))
  }

  protected case class ToJsonFunctionGenerator(obj:AnyRef)(implicit formats:Formats) extends LiftAjaxFunctionGenerator {
    def toAnonFunc = AnonFunc(JsReturn(JsRaw(stringify(obj))))
  }

  protected case class FromAnyFunctionGenerator(obj:Any)(implicit formats:Formats) extends LiftAjaxFunctionGenerator {
    def toAnonFunc = AnonFunc(JsReturn(JsRaw(stringify(obj))))
  }

  trait AjaxFunctionGenerator {

    def moduleDependencies: Set[String]

    def serviceDependencies: Set[String]

    def toAnonFunc: AnonFunc
  }

  trait LiftAjaxFunctionGenerator extends AjaxFunctionGenerator {

    def moduleDependencies: Set[String] = Set("lift-ng")

    def serviceDependencies: Set[String] = Set("liftProxy")

    private val SuccessField = "success"
    private val FutureField = "futureId"

    protected def promiseToJson(promise: Promise): JsObj = {
      promise match {
        case Resolve(Some(jsExp), _) => JsObj(SuccessField -> JsTrue, "data" -> jsExp)
        case Resolve(None, futureId) => futureId.map(
          id => JsObj(SuccessField -> JsTrue, FutureField -> id)
        ).getOrElse(JsObj(SuccessField -> JsTrue))
        case Reject(reason) => JsObj(SuccessField -> JsFalse, "msg" -> reason)
      }
    }

    protected def invalidJson(json:String) = {
      logger.warn("Received invalid JSON from the client => "+json)
      "invalid json"
    }
  }

  /**
   * A model to be sent from angularjs as json, to lift deserialized into this class.
   */
  trait NgModel

  // These case classes encapsulate the incoming request. We used to have an id field, which was the impetus
  // for having explicit classes. Now they're just a hold over/placeholder in case we need anything like this in
  // the future. Consider refactoring and removing these...
  case class RequestData[Model <: NgModel : Manifest](data:Model)
  case class RequestString(data:String)

  case class ReturnData(id:FutureId, json:Box[String])

  type FutureId = String
  val FutureIdNA:FutureId = ""
  type NgFuture[T <: Any] = (LAFuture[Box[T]], FutureId)
}

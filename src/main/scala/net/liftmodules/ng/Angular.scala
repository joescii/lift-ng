package net.liftmodules.ng

import java.util.concurrent.{ConcurrentHashMap, ConcurrentMap}

import scala.collection.mutable
import scala.xml.{Elem, NodeSeq}
import net.liftweb.actor.LAFuture
import net.liftweb.common._
import net.liftweb.json._
import net.liftweb.http.{DispatchSnippet, LiftRules, RequestVar, ResourceServer, S, SessionVar}
import net.liftweb.http.js.JE._
import net.liftweb.http.js.JsCmds._
import net.liftweb.http.js.{JsCmd, JsExp}
import net.liftweb.json.JsonAST.{JNull, JObject, JString}
import net.liftweb.util.Props
import net.liftweb.util.Props.RunModes
import net.liftweb.util.StringHelpers._

import scala.concurrent.{ExecutionContext, Future}

/**
 * Primary lift-ng module
 */
object Angular extends DispatchSnippet with AngularProperties with LiftNgJsHelpers with Loggable {
  val defaultFailureHandler: Failure => Reject = { f =>
    val msg = encJs(f.msg).drop(1).dropRight(1) // Encode into valid JS, but strip the quotes it adds
    Reject(JString(msg))
  }

  private [ng] var futuresDefault: Boolean = true
  private [ng] var appSelectorDefault: String = "[ng-app]"
  private [ng] var includeJsScript: Boolean = true
  private [ng] var includeAngularJs: Boolean = false
  private [ng] var additionalAngularJsModules: Seq[String] = Seq()
  private [ng] var includeAngularCspCss: Boolean = false
  private [ng] var retryAjaxInOrder: Boolean = false
  private [ng] var failureHandler: Failure => Reject = defaultFailureHandler
  private [ng] def rand = "NG"+randomString(18)

  /**
    * Init function to be called in Boot
    * @param futures true to include {{{net.liftweb.actor.LAFuture}}} support (and hence add a comet to your page), false otherwise
    * @param appSelector the CSS selector to find your app in the page
    * @param includeJsScript true to include the prerequisite liftproxy.js file, false if you plan to include it yourself.
    * @param includeAngularJs true to include angular.js and modules found in angularjs webjar.
    * @param additionalAngularJsModules list of additional angularjs modules to include in the page.
    * @param includeAngularCspCss true to include angular-csp.css found in angularjs webjar.
    * @param retryAjaxInOrder true to preserve the order of ajax service calls even in the event of server communication failures.
    * @param failureHandler fn for converting a Lift Failure into a $q promise rejection on the client.
    */
  def init(
    futures: Boolean = true,
    appSelector: String = "[ng-app]",
    includeJsScript: Boolean = true,
    includeAngularJs: Boolean = false,
    additionalAngularJsModules: List[String] = List(),
    includeAngularCspCss: Boolean = false,
    retryAjaxInOrder: Boolean = false,
    failureHandler: Failure => Reject = defaultFailureHandler
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

    this.includeAngularJs = includeAngularJs
    this.includeAngularCspCss = includeAngularCspCss
    if(includeAngularJs || includeAngularCspCss) {
      this.additionalAngularJsModules = additionalAngularJsModules
      AngularJsRest.init()
    }

    this.retryAjaxInOrder = retryAjaxInOrder

    // TODO Remove limitation
    require(
      !retryAjaxInOrder || !BuildInfo.liftEdition.startsWith("3"),
      "retryAjaxInOrder is not currently supported in Lift 3.x"
    )

    this.failureHandler = failureHandler
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
        def newBinder = theClass.newInstance.asInstanceOf[NgModelBinder[Any]]

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

  private object ToServerBinders extends SessionVar[ConcurrentMap[String, NgModelBinder[Any]]](new ConcurrentHashMap())
  private def addToServerBinder(theType:String, b:NgModelBinder[Any]):NgModelBinder[Any] = {
    ToServerBinders.get.put(theType, b)
    b
  }
  def getToServerBinder(theType:String):Box[NgModelBinder[Any]] = Option(ToServerBinders.get.get(theType))

  /**
   * Renders all the modules that have been added to the RequestVar.
   */
  def render: NodeSeq = {
    // We should only call this once from the <head> tag. Calling it again indicates a programming error.
    require(!HeadRendered.is, "render has already been called once")

    HeadRendered.set(true)

    val liftproxy = if(includeJsScript) <script src={liftproxySrc}></script> else NodeSeq.Empty
    val jsModule = Script(JsRaw(
      "var net_liftmodules_ng=net_liftmodules_ng||{};" +
      "net_liftmodules_ng.ajax=function(){"+ajaxFn+".apply(this,arguments)};" +
      "net_liftmodules_ng.retryAjaxInOrder="+retryAjaxInOrder+";" +
      "net_liftmodules_ng.enhancedAjax="+enhancedAjax+";" +
      "net_liftmodules_ng.version=\"" + BuildInfo.version + "\";" +
      "net_liftmodules_ng.jsPath=\"" + liftproxySrc +"\";"
    ))
    val modules = Script(AngularModules.is.map(_.cmd).reduceOption(_ & _).getOrElse(Noop))
    val includeFutures = S.attr("futures").map(bool(_, futuresDefault)).openOr(futuresDefault)
    val futureActor = if(includeFutures) <div data-lift="comet?type=LiftNgFutureActor"></div> else NodeSeq.Empty

    angularCspCss ++ angularModules ++ liftproxy ++ jsModule ++ modules ++ futureActor
  }

  private def angularModules:NodeSeq = if(includeAngularJs) {
    val ms  = S.attr("additional-angularjs-modules").map(_.split(',').map(_.trim).toSeq).openOr(additionalAngularJsModules)
    val notDev = Props.mode != RunModes.Development
    val min = S.attr("min").map(bool(_, notDev)).openOr(notDev)
    ("" +: ms).map { m =>
      val name = if(m == "") "angular" else "angular-"+m
      val id = name+"_js"
      val src = AngularJsRest.path + AngularJsRest.version + "/" + name + (if(min) ".min" else "") + ".js"
      <script id={id} src={src} type="text/javascript"></script>
    }.foldLeft(NodeSeq.Empty)(_ ++ _)
  } else NodeSeq.Empty

  private lazy val angularCspCss:NodeSeq = if(!includeAngularCspCss) NodeSeq.Empty else
    <link id="angular-csp_css" href={AngularJsRest.path + AngularJsRest.version + "/angular-csp.css"} rel="stylesheet"/>

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

  private [ng] def plumbFuture[T <: Any](f: FutureBox[T], id: String)(implicit formats: Formats, ec: ExecutionContext): FutureBox[T] = {
    S.session map { s => f foreach { box =>
      // TODO: Address this deprecation warning once support for Lift 3.0.x is dropped
      s.sendCometActorMessage("LiftNgFutureActor", Empty, ReturnData(id, box, formats))
    }}
    f
  }

  private [ng] def handleFailure(f: Failure): Reject = failureHandler(f)

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

    private[Angular] def toGenerator: JsExp
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
  class JsObjFactory() extends Factory {
    /**
     * name -> function
     */
    @transient
    private val functions = mutable.HashMap.empty[String, AjaxFunctionGenerator]

    override private[Angular] def moduleDependencies =
      functions.values.foldLeft(Set.newBuilder[String])(_ ++= _.moduleDependencies).result()

    @transient
    private val promiseMapper = DefaultApiSuccessMapper

    /**
     * Registers a no-arg javascript function in this service's javascript object that returns a \$q promise.
     *
     * @param functionName name of the function to be made available on the service/factory
     * @param func produces the result of the ajax call. Failure, Full(DefaultResponse(false)), and some other logical
     *             failures will be mapped to promise.reject(). See promiseMapper.
     */
    def defAny
      (functionName: String, func: => Box[Any])
      (implicit formats: Formats)
      : JsObjFactory =
      registerFunction(functionName, AjaxNoArgToJsonFunctionGenerator(Unit => promiseMapper.boxToPromise(func)))

    /**
      * Registers a javascript function in this service's javascript object that takes an arbitrary parameter object and returns a
      * \$q promise.
      *
      * @param functionName name of the function to be made available on the service/factory
      * @param func produces the result of the ajax call. Failure, Full(DefaultResponse(false)), and some other logical
      *             failures will be mapped to promise.reject(). See promiseMapper.
      */
    def defParamToAny[P]
    (functionName: String, func: P => Box[Any])
    (implicit mf: Manifest[P], formats: Formats)
    : JsObjFactory =
      registerFunction(functionName, AjaxJsonToJsonFunctionGenerator(func.andThen(promiseMapper.boxToPromise(_))))

    /**
      * Registers a no-arg javascript function in this service's javascript object that returns a \$q promise.
      *
      * @param functionName name of the function to be made available on the service/factory
      * @param func produces the result of the ajax call. Failure, Full(DefaultResponse(false)), and some other logical
      *             failures will be mapped to promise.reject(). See promiseMapper.
      */
    def defFutureAny[T <: Any]
    (functionName: String, func: => Future[T])
    (implicit formats: Formats, ec: ExecutionContext)
    : JsObjFactory =
      registerFunction(functionName, NoArgFutureFunctionGenerator(Unit => func))

    /**
      * Registers a javascript function in this service's javascript object that takes an arbitrary parameter object and returns a
      * \$q promise.
      *
      * @param functionName name of the function to be made available on the service/factory
      * @param func produces the result of the ajax call. Failure, Full(DefaultResponse(false)), and some other logical
      *             failures will be mapped to promise.reject(). See promiseMapper.
      */
    def defParamToFutureAny[P, T <: Any]
    (functionName: String, func: P => Future[T])
    (implicit mf: Manifest[P], formats: Formats, ec: ExecutionContext)
    : JsObjFactory =
      registerFunction(functionName, JsonFutureFunctionGenerator(func))

    /**
      * Registers a no-arg javascript function in this service's javascript object that returns an AnyVal value.
      * Use this to provide primitive values which are known at page load time and do not change.
      *
      * @param functionName name of the function to be made available on the service/factory
      * @param value value to be returned on invocation of this function in the client.
      */
    def valAny
    (functionName: String, value:Any)
    (implicit formats: Formats)
    : JsObjFactory =
      registerFunction(functionName, FromAnyFunctionGenerator(value))


    /**
     * Registers a no-arg javascript function in this service's javascript object that returns a \$q promise.
     *
     * @param functionName name of the function to be made available on the service/factory
     * @param func produces the result of the ajax call. Failure, Full(DefaultResponse(false)), and some other logical
     *             failures will be mapped to promise.reject(). See promiseMapper.
     */
    @deprecated(message = "jsonCall has been replaced by defAny", since = "0.11.0")
    def jsonCall
    (functionName: String, func: => Box[AnyRef])
    : JsObjFactory = defAny(functionName, func)(DefaultFormats)

    /**
     * Registers a javascript function in this service's javascript object that takes a String and returns a \$q promise.
     *
     * @param functionName name of the function to be made available on the service/factory
     * @param func produces the result of the ajax call. Failure, Full(DefaultResponse(false)), and some other logical
     *             failures will be mapped to promise.reject(). See promiseMapper.
     */
    @deprecated(message = "defStringToAny has been replaced by defParamToAny", since = "0.11.0")
    def defStringToAny
      (functionName: String, func: String => Box[Any])
      (implicit formats:Formats = DefaultFormats)
      : JsObjFactory = defParamToAny(functionName, func)

    /**
     * Registers a javascript function in this service's javascript object that takes an arbitrary parameter object and returns a
     * \$q promise.
     *
     * @param functionName name of the function to be made available on the service/factory
     * @param func produces the result of the ajax call. Failure, Full(DefaultResponse(false)), and some other logical
     *             failures will be mapped to promise.reject(). See promiseMapper.
     */
    @deprecated(message = "defModelToAny has been replaced by defParamToAny", since = "0.11.0")
    def defModelToAny[P]
      (functionName: String, func: P => Box[Any])
      (implicit mf:Manifest[P], formats:Formats = DefaultFormats)
      : JsObjFactory = defParamToAny(functionName, func)

    /**
     * Registers a javascript function in this service's javascript object that takes an arbitrary parameter object and returns a
     * \$q promise.
     *
     * @param functionName name of the function to be made available on the service/factory
     * @param func produces the result of the ajax call. Failure, Full(DefaultResponse(false)), and some other logical
     *             failures will be mapped to promise.reject(). See promiseMapper.
     */
    @deprecated(message = "jsonCall has been replaced by defParamToAny", since = "0.11.0")
    def jsonCall[P]
      (functionName: String, func: P => Box[Any])
      (implicit mf:Manifest[P])
      : JsObjFactory = defParamToAny(functionName, func)(mf, DefaultFormats)

    /**
     * Registers a no-arg javascript function in this service's javascript object that returns a \$q promise.
     *
     * @param functionName name of the function to be made available on the service/factory
     * @param func produces the result of the ajax call. Failure, Full(DefaultResponse(false)), and some other logical
     *             failures will be mapped to promise.reject(). See promiseMapper.
     */
    @deprecated(message = "future has been replaced by defFutureAny", since = "0.11.0")
    def future[T <: Any]
      (functionName: String, func: => LAFuture[Box[T]])
      : JsObjFactory =
      registerFunction(functionName, NoArgLAFutureFunctionGenerator(Unit => func)(DefaultFormats, AngularExecutionContext.ec))

    /**
      * Registers a no-arg javascript function in this service's javascript object that returns a \$q promise.
      *
      * @param functionName name of the function to be made available on the service/factory
      * @param func produces the result of the ajax call. Failure, Full(DefaultResponse(false)), and some other logical
      *             failures will be mapped to promise.reject(). See promiseMapper.
      */
    @deprecated(message = "defFutureAny(LAFuture[Box[T]]) has been replaced by defFutureAny(Future[T])", since = "0.11.0")
    def defFutureAny[T <: Any]
    (functionName: String, func: => LAFuture[Box[T]])
    (implicit formats: Formats = DefaultFormats)
    : JsObjFactory =
      registerFunction(functionName, NoArgLAFutureFunctionGenerator(Unit => func)(formats, AngularExecutionContext.ec))

    /**
     * Registers a javascript function in this service's javascript object that takes a String and returns a \$q promise.
     *
     * @param functionName name of the function to be made available on the service/factory
     * @param func produces the result of the ajax call. Failure, Full(DefaultResponse(false)), and some other logical
     *             failures will be mapped to promise.reject(). See promiseMapper.
     */
    @deprecated(message = "defStringToFutureAny has been replaced by defParamToFutureAny", since = "0.11.0")
    def defStringToFutureAny[T <: Any]
      (functionName: String, func: String => LAFuture[Box[T]])
      (implicit formats: Formats = DefaultFormats)
      : JsObjFactory = {
        implicit val ec = AngularExecutionContext.ec
        registerFunction(functionName, JsonLAFutureFunctionGenerator(func))
      }

    /**
     * Registers a javascript function in this service's javascript object that takes an arbitrary parameter object and returns a
     * \$q promise.
     *
     * @param functionName name of the function to be made available on the service/factory
     * @param func produces the result of the ajax call. Failure, Full(DefaultResponse(false)), and some other logical
     *             failures will be mapped to promise.reject(). See promiseMapper.
     */
    @deprecated(message = "defModelToFutureAny has been replaced by defParamToFutureAny", since = "0.11.0")
    def defModelToFutureAny[P, T <: Any]
      (functionName: String, func: P => LAFuture[Box[T]])
      (implicit mf: Manifest[P], formats: Formats = DefaultFormats)
      : JsObjFactory =
      registerFunction(functionName, JsonLAFutureFunctionGenerator(func)(mf, formats, AngularExecutionContext.ec))

    /**
     * Registers a javascript function in this service's javascript object that takes an arbitrary parameter object and returns a
     * \$q promise.
     *
     * @param functionName name of the function to be made available on the service/factory
     * @param func produces the result of the ajax call. Failure, Full(DefaultResponse(false)), and some other logical
     *             failures will be mapped to promise.reject(). See promiseMapper.
     */
    @deprecated(message = "future has been replaced by defParamToFutureAny", since = "0.11.0")
    def future[P, T <: Any]
      (functionName: String, func: P => LAFuture[Box[T]])
      (implicit mf:Manifest[P])
      : JsObjFactory = defModelToFutureAny(functionName, func)

    /**
     * Registers a no-arg javascript function in this service's javascript object that returns a String value.
     * Use this to provide string values which are known at page load time and do not change.
     *
     * @param functionName name of the function to be made available on the service/factory
     * @param value value to be returned on invocation of this function in the client.
     */
    @deprecated(message = "string has been replaced by valAny", since = "0.11.0")
    def string
      (functionName: String, value:String)
      (implicit formats:Formats = DefaultFormats)
      : JsObjFactory = valAny(functionName, value)

    /**
     * Registers a no-arg javascript function in this service's javascript object that returns an AnyVal value.
     * Use this to provide primitive values which are known at page load time and do not change.
     *
     * @param functionName name of the function to be made available on the service/factory
     * @param value value to be returned on invocation of this function in the client.
     */
    @deprecated(message = "anyVal has been replaced by valAny", since = "0.11.0")
    def anyVal
      (functionName: String, value:AnyVal)
      (implicit formats:Formats = DefaultFormats)
      : JsObjFactory = valAny(functionName, value)

    /**
     * Registers a no-arg javascript function in this service's javascript object that returns a json object.
     * Use this to provide objects which are known at page load time and do not change.
     *
     * @param functionName name of the function to be made available on the service/factory
     * @param value value to be returned on invocation of this function in the client.
     */
    @deprecated(message = "json has been replaced by valAny", since = "0.11.0")
    def json
      (functionName: String, value:AnyRef)
      (implicit formats:Formats = DefaultFormats)
      : JsObjFactory = valAny(functionName, value)

    /**
     * Adds the ajax function factory and its dependencies to the factory.
     */
    private def registerFunction(functionName: String, generator: AjaxFunctionGenerator): JsObjFactory = {
      require(functionName.nonEmpty)
      functions += functionName -> generator
      this
    }

    private[Angular] def toGenerator: JsExp = {
      val serviceDependencies = functions.values.foldLeft(Set.newBuilder[String])(_ ++= _.serviceDependencies).result().toList
      val annotations = serviceDependencies.map(Str.apply)
      val fn = AnonFunc(serviceDependencies.mkString(","), JsReturn(JsObj(functions.mapValues(_.toAnonFunc).toSeq: _*)))
      JsArray(annotations :+ fn)
    }

  }

  /**
   * Maps an api result to a Promise object that will be used to fulfill the javascript promise object.
   */
  object DefaultApiSuccessMapper extends PromiseMapper {
    import scala.util.{Try, Success}

    private [this] def resolve(any: Any, formats: Formats) = Resolve(Some(Extraction.decompose(any)(formats + new LAFutureSerializer)))

    override def tryToPromise(t: => Try[Any])(implicit formats: Formats): Promise = try {
      t match {
        case Success(Unit) => Resolve()
        case Success(any: Any) => resolve(any, formats)
        case f: scala.util.Failure[Any] => handleFailure(throwableToFailure(f.exception))
      }
    } catch {
      case t: Throwable =>
        handleFailure(throwableToFailure(t))
    }

    override def boxToPromise(box: => Box[Any])(implicit formats: Formats): Promise = try {
      box match {
        case Full(Unit) | Empty => Resolve()
        case Full(any: Any) => resolve(any, formats)
        case f: Failure => handleFailure(f)
      }
    } catch {
      case t: Throwable =>
        handleFailure(throwableToFailure(t))
    }
  }

  /**
   * Maps the response passed into the ajax calls into something that can be passed into promise.resolve(data) or
   * promise.reject(reason).
   */
  trait PromiseMapper {
    def boxToPromise(box: => Box[Any])(implicit formats: Formats): Promise
    def tryToPromise(t: => scala.util.Try[Any])(implicit formats: Formats): Promise
  }

  /**
   * Used to resolve or reject a javascript angular \$q promise.
   */
  sealed trait Promise

  case class Resolve(data: Option[JValue] = None, futureId: Option[String] = None) extends Promise

  case class Reject(data: JValue = JNull) extends Promise

  object Promise {

    def apply(success: Boolean): Promise = if (success) Resolve(None) else Reject()
  }

  protected case class AjaxNoArgToJsonFunctionGenerator(jsFunc: Unit => Promise) extends LiftAjaxFunctionGenerator {

    def toAnonFunc = AnonFunc(JsReturn(Call("liftProxy.request", liftPostData)))

    private def liftPostData = SHtmlExtensions.ajaxJsonPost((id) => promiseToJson(tryPromise((), jsFunc)))
  }

  protected case class AjaxStringToJsonFunctionGenerator(stringToPromise: (String) => Promise)(implicit formats:Formats)
    extends LiftAjaxFunctionGenerator {

    private val ParamName = "str"

    def toAnonFunc = AnonFunc(ParamName, JsReturn(Call("liftProxy.request", liftPostData)))

    private def liftPostData = SHtmlExtensions.ajaxJsonPost(JsVar(ParamName), jsonFunc)

    private def jsonFunc: String => JObject = {
      val jsonToPromise = (json: String) => JsonParser.parse(json).extractOpt[RequestString] match {
        case Some(RequestString(data)) => tryPromise(data, stringToPromise)
        case None => handleFailure(invalidJson(json))
      }
      jsonToPromise andThen promiseToJson
    }
  }

  protected case class AjaxJsonToJsonFunctionGenerator[P](modelToPromise: P => Promise)(implicit mf:Manifest[P], formats:Formats)
    extends LiftAjaxFunctionGenerator {
    private val ParamName = "json"

    def toAnonFunc = AnonFunc(ParamName, JsReturn(Call("liftProxy.request", liftPostData)))

    private def liftPostData: JsExp = SHtmlExtensions.ajaxJsonPost(JsVar(ParamName), jsonFunc)

    private def jsonFunc: String => JObject = {
      val jsonToPromise = (json: String) => Json.slash(JsonParser.parse(json), ("data")).extractOpt[P] match {
        case Some(model) => tryPromise(model, modelToPromise)
        case None => handleFailure(invalidJson(json))
      }
      jsonToPromise andThen promiseToJson
    }
  }

  protected abstract class FutureFunctionGenerator extends LiftAjaxFunctionGenerator {
    protected def jsonFunc[T <: Any](jsonToFuture: (String) => NgFuture[T])(implicit formats: Formats): String => JObject = {
      val futureToJObject: NgFuture[T] => JObject = { case (future, id) =>
        future.value match {
          case Some(t) => promiseToJson(DefaultApiSuccessMapper.tryToPromise(t))
          case _ => promiseToJson(Resolve(None, Some(id)))
        }
      }

      jsonToFuture andThen futureToJObject
    }

    protected def reject[T <: Any](json: String): NgFuture[T] = {
      (Future.successful(invalidJson(json)), FutureIdNA)
    }
  }

  protected case class NoArgLAFutureFunctionGenerator[T <: Any](func: Unit => LAFuture[Box[T]])(implicit formats: Formats, ec: ExecutionContext) extends FutureFunctionGenerator {
    def toAnonFunc = AnonFunc(JsReturn(Call("liftProxy.request", liftPostData)))

    private def liftPostData = SHtmlExtensions.ajaxJsonPost(jsonFunc(jsonToFuture))

    val jsonToFuture:(String) => NgFuture[T] = json => {
      val id = rand
      (Angular.plumbFuture(tryLAFuture((), func), id), id)
    }
  }

  protected case class JsonLAFutureFunctionGenerator[P, T <: Any](func: P => LAFuture[Box[T]])(implicit mf: Manifest[P], formats: Formats, ec: ExecutionContext) extends FutureFunctionGenerator {
    private val ParamName = "json"

    def toAnonFunc = AnonFunc(ParamName, JsReturn(Call("liftProxy.request", liftPostData)))

    private def liftPostData = SHtmlExtensions.ajaxJsonPost(JsVar(ParamName), jsonFunc(jsonToFuture))

    def jsonToFuture:(String) => NgFuture[T] = json => {
      val dataOpt = Json.slash(JsonParser.parse(json), "data").extractOpt[P]
      val id = rand

      val fOpt = for {
        data <- dataOpt
      } yield {
        (Angular.plumbFuture(tryLAFuture(data, func), id), id)
      }

      fOpt.openOr(reject[T](json))
    }
  }

  protected case class NoArgFutureFunctionGenerator[T <: Any](func: Unit => Future[T])(implicit formats: Formats, ec: ExecutionContext) extends FutureFunctionGenerator {
    def toAnonFunc = AnonFunc(JsReturn(Call("liftProxy.request", liftPostData)))

    private def liftPostData = SHtmlExtensions.ajaxJsonPost(jsonFunc(jsonToFuture))

    val jsonToFuture:(String) => NgFuture[T] = json => {
      val id = rand
      (Angular.plumbFuture(tryFuture((), func), id), id)
    }
  }

  protected case class JsonFutureFunctionGenerator[P, T <: Any](func: P => Future[T])(implicit mf: Manifest[P], formats: Formats, ec: ExecutionContext) extends FutureFunctionGenerator {
    private val ParamName = "json"

    def toAnonFunc = AnonFunc(ParamName, JsReturn(Call("liftProxy.request", liftPostData)))

    private def liftPostData = SHtmlExtensions.ajaxJsonPost(JsVar(ParamName), jsonFunc(jsonToFuture))

    def jsonToFuture:(String) => NgFuture[T] = json => {
      val dataOpt = Json.slash(JsonParser.parse(json), "data").extractOpt[P]
      val id = rand

      val fOpt = for {
        data <- dataOpt
      } yield {
        (Angular.plumbFuture(tryFuture(data, func), id), id)
      }

      fOpt.openOr(reject[T](json))
    }
  }

  protected case class ToStringFunctionGenerator(s:String)(implicit formats: Formats) extends LiftAjaxFunctionGenerator {
    def toAnonFunc = AnonFunc(JsReturn(s))
  }

  protected case class ToJsonFunctionGenerator(obj:AnyRef)(implicit formats: Formats) extends LiftAjaxFunctionGenerator {
    def toAnonFunc = AnonFunc(JsReturn(JsRaw(stringify(obj))))
  }

  protected case class FromAnyFunctionGenerator(obj:Any)(implicit formats: Formats) extends LiftAjaxFunctionGenerator {
    def toAnonFunc = AnonFunc(JsReturn(JsRaw(stringify(obj))))
  }

  trait AjaxFunctionGenerator {

    def moduleDependencies: Set[String]

    def serviceDependencies: Set[String]

    def toAnonFunc: AnonFunc
  }

  trait LiftAjaxFunctionGenerator extends AjaxFunctionGenerator {
    import FutureConversions._

    def moduleDependencies: Set[String] = Set("lift-ng")

    def serviceDependencies: Set[String] = Set("liftProxy")

    protected def tryPromise[A](a: => A, f: A => Promise): Promise =
      try {
        f(a)
      } catch {
        case t: Throwable =>
          handleFailure(throwableToFailure(t))
      }

    protected def tryFuture[A, T <: Any](a: => A, f: A => Future[T])(implicit ec: ExecutionContext): FutureBox[T] =
      try {
        f(a).boxed
      } catch {
        case t: Throwable =>
          Future.failed(t)
      }

    protected def tryLAFuture[A, T <: Any](a: => A, f: A => LAFuture[Box[T]]): FutureBox[T] =
      try {
        LAFutureToFuture(f(a))
      } catch {
        case t: Throwable =>
          Future.failed(t)
      }

    protected def invalidJson(json: String): Failure = {
      logger.warn("Received invalid JSON from the client => "+json)
      Failure("invalid json")
    }
  }

  /**
   * A model to be sent from angularjs as json, to lift deserialized into this class.
   */
  @deprecated("Using this trait is no longer necessary. You can use the classes or even primitive types directly", "0.11.0")
  trait NgModel


  // These case classes encapsulate the incoming request. We used to have an id field, which was the impetus
  // for having explicit classes. Now they're just a hold over/placeholder in case we need anything like this in
  // the future. Consider refactoring and removing these...
  case class RequestData[P : Manifest](data:P)
  case class RequestString(data:String)

  case class ReturnData[T <: Any](id:FutureId, response:Box[T], formats: Formats)

  type FutureId = String
  val FutureIdNA:FutureId = ""
  type NgFuture[T <: Any] = (FutureBox[T], FutureId)
}

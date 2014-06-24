# lift-ng

AngularJS support for Lift

**lift-ng** is a liftmodule for easing the utilization of [AngularJS](http://docs.angularjs.org/guide/overview) in a Lift application.  The basic premise is to make it a cinch to create server back-end services for injection into your AngularJS components.  This has resulted in the creation of a Scala DSL to closely emulate the way you would define a service in JavaScript.  For a taste, compare this JavaScript...

```javascript
angular.module('lift.pony', [])
  .factory("ponyService", function() {
    return {
      getBestPony: function(arg) {
        // Return the best pony (client-side)
        return BestPony;
      }
    };
  });
```

... to this Scala **lift-ng**

```scala
angular.module("lift.pony")
  .factory("ponyService", jsObjFactory()
    .jsonCall("getBestPony", (arg) => {
      // Return the best pony (server-side)
      Full(BestPony)
    })
  )
```

Both will create an angular module named `lift.pony` with a service named `ponyService` with a function named `getBestPony`, yet the former runs on the client-side, and the latter runs on the server-side.

In addition to supporting client-side initiated service requests, **lift-ng** supports pushes from the server via [comet](#comet).  For this, you will have a server-side actor with access to the scope of the companion client-side DOM element:

```scala
class PushPonies extends AngularActor {
  override def lowPriority = {
    case best:Pony =>
      rootScope.emit("emit-pony", best)
      rootScope.broadcast("broadcast-pony", best)
      rootScope.assign("best.pony", best)
      scope.emit("emit-pony", best)
      scope.broadcast("broadcast-pony", best)
      scope.assign("best.pony", best)
  }
}
```

## Tutorial

We recommend that you view original contributor [Doug Roper](https://github.com/htmldoug)'s [youtube demo](http://www.youtube.com/watch?v=VH-S2UDN-NQ) of the functionality of this plugin.  See also the [sample project](https://github.com/htmldoug/ng-lift-proxy) as seen in the youtube video.

## Configuration

Add the Sonatype.org Releases repo as a resolver in your `build.sbt` or `Build.scala` as appropriate.

```scala
resolvers += "Sonatype.org Releases" at "https://oss.sonatype.org/content/repositories/releases/"
```

Add this `lift-ng` as a dependency in your `build.sbt` or `Build.scala` as appropriate.

```scala
libraryDependencies ++= {
  val liftEdition = "2.5" // Also supported: "2.6" and "3.0"

  Seq(
    // Other dependencies ...
    "net.liftmodules" %% ("ng_"+liftEdition) % "0.4.2" % "compile"
  )
}
```

And invoke `Angular.init()` in your `Boot` class.

```scala
package bootstrap.liftweb

class Boot {
  def boot {
    // Other stuff...
    
    net.liftmodules.ng.Angular.init(
      // Set to true if you plan to use futures. False otherwise to avoid an unneeded comet
      futures = true,

      // Set to the CSS selector for finding your apps in the page.
      appSelector = "[ng-app]"
    )
  }
}
```

## Supported Versions

**lift-ng** is built and released to support Lift editions 2.5 and 2.6 with Scala versions 2.9.1, 2.9.1-1, 2.9.2, and 2.10; and Lift edition 3.0 with Scala version 2.10.3.  This project's scala version is purposefully set at the lowest common denominator to ensure each version compiles.  It has been developed against Angular 1.1.5+

## Usage

Below are a few usage examples.  Be sure to check out the aforementioned [sample project](https://github.com/htmldoug/ng-lift-proxy) or the [test project](https://github.com/joescii/lift-ng/tree/master/test-project) for fully functional examples

### AJAX

Continuing with the sample code from the introduction, you will need a snippet which contains the definition of the angular service/factory which can be called from the client code.

```scala
object NgPonyService {
  def render = renderIfNotAlreadyDefined(
    angular.module("lift.pony")
      .factory("ponyService", jsObjFactory()
        .jsonCall("getBestPony", (arg) => {
          // Return the best pony (server-side)
          try {
            Full(BestPony)
          } catch {
            case e:Exception => Failure(e.getMessage)
          }
        })
      )
  )
}
```

This `renderIfNotAlreadyDefined` returns a `scala.xml.NodeSeq`.  Hence you will need to add script tags to your target HTML page(s) for the services as well as some plumbing from this plugin.  

```html
<!-- The angular library (manually) -->
<script type="text/javascript" src="/scripts/angular.js"></script>

<!-- Or better yet, via lift-ng-js. (https://github.com/joescii/lift-ng-js) -->
<script data-lift="AngularJS"></script>

<!-- Prerequisite stuff the plugin needs -->
<script data-lift="Angular"></script>

<!-- The NgPonyService snippet defined above -->
<script data-lift="NgPonyService"></script>

<!-- Your angular controllers, etc -->
<script type="text/javascript" src="/scripts/pony.js"></script>
```

The resulting angular service returns a `$q` promise (see [AngularJS: ng.$q](http://docs.angularjs.org/api/ng.$q)).  When you call the service, you register callbacks for success, error, and notify (not currently utilized).

```javascript
angular.module('pony', ['lift.pony'])
  .controller('PonyCtrl', function ($scope, ponyService) {
    $scope.onClick = function () {
      ponyService.getBestPony().then(function(pony) {
        // We have our pony!
        $scope.pony = pony;
      },
      function(err) {
        // No pony!
        $scope.error = err;
      },
      function(progress) {
        // We're still working on getting that pony...
        // Well, not really... we don't use this today...
      });
    };
  });
```

#### No arguments, string arguments, or case class arguments

Just like with Lift's `SHtml.ajaxInvoke`, you can make a service which takes no arguments.  Hence we could have defined our `ponyService.getBestPony` like the following...

```scala
angular.module("lift.pony")
  .factory("ponyService", jsObjFactory()
    .jsonCall("getBestPony", {
      // Return the best pony (server-side)
      try {
        Full(BestPony)
      } catch {
        case e:Exception => Failure("No Pony!")
      }
    })
  )
```

Or we can accept a `String`...

```scala
angular.module("lift.pony")
  .factory("ponyService", jsObjFactory()
    .jsonCall("getPonyByName", (name:String) => {
      // Return the matching pony
      try {
        Full(BestPony)
      } catch {
        case e:Exception => Failure("No Pony!")
      }
    })
  )
```

Finally, perhaps most importantly, we expect a case class to be sent to the server.  Note that the case class must extend `NgModel` for this to work.

```scala
case class Pony (name:String, img:URL) extends NgModel

angular.module("lift.pony")
  .factory("ponyService", jsObjFactory()
    .jsonCall("setBestPony", (pony:Pony) => {
      // Nothing to return
      Empty
    })
  )
```

#### Multiple function calls

All of the above functions can be part of the same service...
```scala
angular.module("lift.pony")
  .factory("ponyService", jsObjFactory()
    .jsonCall("getBestPony", {
      // Return the best pony (server-side)
      Full(BestPony)
    })

    .jsonCall("getPonyByName", (name:String) => {
      // Return the matching pony
      Full(MyPony)

    .jsonCall("setBestPony", (pony:Pony) => {
      // Nothing to return
      Empty
    })
  )
```

#### Futures
All of the examples thus far have assumed the value can be calculated quickly without expensive blocking or asynchronous calls.  Since it is quite common to perform expensive operations or call APIs which return a `Future[T]`, it is important that **lift-ng** likewise supports returning an `LAFuture`.

The same signatures for `jsonCall` are supported for futures:

```scala
angular.module("lift.pony")
  .factory("ponyService", jsObjFactory()
    .future("getBestPony", {
      // Create the future
      val f = new LAFuture[Box[Pony]]()
      // Do something to get the result
      futurePonyGetter(f)
      // Return the future that will contain a Pony
      f
    })

    .future("getPonyByName", (name:String) => {
      // Return a future containing the matching pony
      futurePonyGetter(name)

    .future("setBestPony", (pony:Pony) => {
      // Nothing to return
      val f = new LAFuture[Box[Pony]]()
      f.satisfy(Empty)
      f
    })
  )
```

Because the underlying Lift library does not currently support returning futures for AJAX calls (as of 2.5.1), we had to circumvent this limitation by utilizing comet.  As a result, if you want to utilize futures in your angular app, we must be able to locate your app in the DOM.  By default, we look for any elements containing the `ng-app` attribute.  This can be overridden in the `Angular.init()` call via the `appSelector` property.  This allows us to hook in to your app via comet and send messages asynchronously back to the lift-proxy service.

#### Testing
Testing services provided by **lift-ng** with Jasmine (etc) can be accomplished in the same manner as you would test any Angular service.

```javascript
describe("pony", function(){
  // Service mock
  var ponyService = {};

  // Handle to the rootScope
  var rootScope = {};

  // Handle to the scope
  var scope = {};

  // Set up the pony module
  beforeEach(function(){module('pony');});

  // Mock out the service
  beforeEach(function() {
    angular.mock.module(function($provide) {
      $provide.value('lift.pony', ponyService);
    });
  });

  // Build the mock with a $q promise
  beforeEach(inject(function($q) {
    ponyService.defer = $q.defer();
    ponyService.getBestPony = function() {
      return this.defer.promise;
    };
  }));

  // Create a controller for each test
  beforeEach(inject(function($rootScope, $controller) {
    rootScope = $rootScope;
    scope = $rootScope.$new();
    $controller('PonyCtrl', {
      $scope: scope,
      ponyService: ponyService
    });
  }));

  // Write a test
  it('should call the service when onClick is called', function() {
    // Before onClick, the pony will be undefined.
    expect(scope.pony).toBeUndefined();

    // Provide a pony to be returned
    var pony = {
      name: 'Doug',
      img: 'doug.jpg'
    };
    ponyService.defer.resolve(pony);

    // Simulate the click
    scope.onClick();

    // This call lets the $q callback happen
    rootScope.$digest();

    // Expect that pony has now been set.
    expect(scope.pony).toEqual(pony);
  });
});
```

In regards to testing the server-side code, we recommend implementing your service logic in a standalone module which handles all of the complex business logic.  Then utilize **lift-ng** to merely expose those services to your angular app.  This way your business logic is decoupled from **lift-ng** and easily testable.

### Non-AJAX
Sometimes the value you want to provide in a service is known at page load time and should not require a round trip back to the server.  Typical examples of this are configuration settings, session values, etc.  To provide a value at page load time, just use `JsonObjFactory`'s `string`, `anyVal`, or `json` methods.

```scala
angular.module("StaticServices")
  .factory("staticService", jsObjFactory()
    .string("string", "FromServer1")
    .anyVal("integer", 42)
    .json("obj", StringInt("FromServer2", 88))

  )
```

The above produces a simple service equivalent to the following JavaScript
```javascript
angular.module("StaticServices",["zen.lift.proxy"])
  .factory("staticService", function(liftProxy) { return {
    string:  function() {return "FromServer1"},
    integer: function() {return "42"},
    obj:     function() {return {str:"FromServer2",num:88}}
  }});
```

### Comet 

Now we can take a look at how to utilize Lift's comet support to asynchronously send angular updates from the server

First you should write a new class which extends the `AngularActor` trait, which is a sub-trait of Lift's `CometActor`.  Thus you can do anything you can normally with a `CometActor`, as well as get access the `$scope` where the actor is defined in the DOM and the `$rootScope` of the angular application.  Currently we support `$emit`, `$broadcast`, and assignment of arbitrary fields on the given scope object.

```scala
class CometExample extends AngularActor {
  override def lowPriority = {
    case ("emit", msg:String) => rootScope.emit("emit-message", msg)
    case ("emit", obj:AnyRef) => rootScope.emit("emit-object",  obj)
    
    case ("broadcast", msg:String) => scope.broadcast("emit-message", msg)
    case ("broadcast", obj:AnyRef) => scope.broadcast("emit-object",  obj)

    case ("assign", msg:String) => rootScope.assign("my.str.field", msg)
    case ("assign", obj:AnyRef) => scope.assign("my.obj.field", obj)
  }
}
```

Now add the comet actor into your HTML DOM within the scope you wish to belong to within the `ng-application`.

```html
<div ng-app="ExampleApp">
  <div data-lift="comet?type=CometExample"></div>
  <!-- other stuff -->
</div>
```

Then do whatever you need in your angular application to listen for events, watch for changes, etc.

```javascript
angular.module('ExampleApp', [])
.controller('ExampleController', ['$rootScope', '$scope', function($rootScope, $scope) {
  $rootScope.$on('emit-message', function(e, msg) {
    $scope.emitMessage = msg;
  });
  $rootScope.$on('emit-object', function(e, obj) {
    $scope.emitObject = obj;
  });
  $scope.$on('broadcast-message', function(e, msg) {
    $scope.broadcastMessage = msg;
  });
  $scope.$on('broadcast-object', function(e, obj) {
    $scope.broadcastObject = obj;
  });
  $rootScope.$watch('my.str.field', function(e, msg) {
    // ...
  });
  $scope.$watch('my.obj.field', function(e, obj) {
    // ...
  });
}]);
```

### i18n Internationalization
If your app doesn't require sophisticated internationalization capabilities (i.e., Java resource bundles will suffice), then you can inject your resource bundles as a service into your app.

Given a resource bundle named `bundleName.properties`:

```text
hello=Howdy!
goodbye=Goodbye, {0}!
```

Add it as a service available to your Angular app with this HTML:

```html
<script id="my-i18n_js"  data-lift="i18n?name=bundleName"></script>
```

Your bundle is made available via the `i18n` module with service/factory name coinciding with the bundle name.  In this example, the object will have a string field named `hello` and a function named `goodbye`:

```javascript
angular.module('ExampleApp', ['i18n'])
.controller('ExampleController', ['$scope', 'bundleName', function($scope, i18n) {
  $scope.hello = i18n.hello;
  $scope.goodbye = i18n.goodbye($scope.username);
}]);
```

You may also specify multiple bundle names.  Here we include the default Lift bundle:

```html
<script id="my-i18n_js"  data-lift="i18n?names=bundleName,lift"></script>
```

Each bundle is another service available in the `i18n` bundle.  Also notice in this example we show keys which aren't valid JavaScript identifiers are also available.

```javascript
angular.module('ExampleApp', ['i18n'])
.controller('ExampleController', ['$scope', 'bundleName', 'lift', function($scope, bundle, lift) {
  $scope.hello = bundle.hello;
  $scope.goodbye = bundle.goodbye($scope.username);
  $scope.lostPasswd = lift["lost.password"];
}]);
```

For more details about this resource bundle object, see [j2js-i18n](https://github.com/joescii/j2js-i18n).

## Scaladocs

The latest version of scaladocs are hosted thanks to [cloudbees](http://www.cloudbees.com/) continuous integration services.  There should not be any differences among the supported versions of Scala.  Nonetheless all are listed here for good measure.
* [Scala 2.10](https://liftmodules.ci.cloudbees.com/job/lift-ng/ws/target/scala-2.10/api/index.html#package)
* [Scala 2.9.2](https://liftmodules.ci.cloudbees.com/job/lift-ng/ws/target/scala-2.9.2/api/index.html#package)
* [Scala 2.9.1-1](https://liftmodules.ci.cloudbees.com/job/lift-ng/ws/target/scala-2.9.1-1/api/index.html#package)
* [Scala 2.9.1](https://liftmodules.ci.cloudbees.com/job/lift-ng/ws/target/scala-2.9.1/api/index.html#package)

## Contributing

As with any open source project, contributions are greatly appreciated.  If you find an issue or have a feature idea, we'd love to know about it!  Any of the following will help this effort tremendously.

1. Issue a Pull Request with the fix/enhancement and unit tests to validate the changes.  OR
2. Issue a Pull Request with failing tests in the [test-project](https://github.com/joescii/lift-ng/tree/master/test-project) to show what needs to be changed OR
3. At a minimum, [open an issue](https://github.com/joescii/lift-ng/issues/new) to let us know about what you've discovered.

### Pull Requests

Below is the recommended procedure for git:

1. Fork it
2. Create your feature branch (`git checkout -b my-new-feature`)
3. Commit your changes (`git commit -am 'Add some feature'`)
4. Push to the branch (`git push origin my-new-feature`)
5. Create new Pull Request

Try to include as much as you are able, such as tests, documentation, updates to this README, etc.

### Testing

Part of contributing your changes will involve testing.  The [test-project](https://github.com/joescii/lift-ng/tree/master/test-project) sub-directory contains and independent sbt project for thoroughly testing the **lift-ng** plugin via selenium.  At a minimum, we ask that you run the tests with your changes to ensure nothing gets inadvertently broken.  If possible, include tests which validate your fix/enhancement in any Pull Requests.

## Wish list

Here are things we would like in this library.  It's not a road map, but should at least give an idea of where we plan to explore soon.

* Support handling parameters of type `json.JValue`.
* Support returning values of type `JsExp`.
* Initial value/first resolve value for services.  The reason for providing a first value will allow the page load to deliver the values rather than require an extra round trip.
* Optional handling for comet events received before Angular has initialized (see [Issue #1](https://github.com/joescii/lift-ng/issues/1))
* 1-way client-server model binding
* Make the DSL prettier
* `AngularActor.scope.parent` support

## Change log

* *0.4.2*: Reverted a change made in 0.4.0 which allows more flexibility in the placement of angular services in the DOM, fixing part 2 of [Issue #2](https://github.com/joescii/lift-ng/issues/2)
* *0.4.1*: Now works for web apps running in a non-root context, fixing [Issue #2](https://github.com/joescii/lift-ng/issues/2).
* *0.4.0*: Now only requires `Angular` snippet in your template(s).
* *0.3.1*: Added i18n service.
* *0.3.0*: Implemented support for a factory/service to return an `LAFuture[Box[T]]`
* *0.2.3*: Implemented `string`, `anyVal`, and `json` on `JsonObjFactory` to allow providing values which are known at page load time and do not otherwise change.
* *0.2.2*: Implemented `AngularActor.assign` for assigning scope variables. `Failure(msg)` message is sent to the client for `$q.reject`. Changed interpretation of `Empty` to mean `Resolve` rather than `Reject`.
* *0.2.1*: Implemented `scope.broadcast` and `scope.emit` for `AngularActor`
* *0.2.0*: Introduction of `AngularActor` featuring `rootScope.broadcast` and `rootScope.emit` as the first comet-backed features
* *0.1.1*: First working release
* *0.1*: First release featuring AJAX services invoked from the client

## License

**lift-ng** is licensed under [APL 2.0](http://www.apache.org/licenses/LICENSE-2.0).

Copyright 2014 net.liftweb

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.


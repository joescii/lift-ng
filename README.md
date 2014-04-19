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

In addition to supporting client-side initiated service requests, **lift-ng** supports pushes from the server via [comet](#comet).

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
    "net.liftmodules" %% ("ng_"+liftEdition) % "0.3.0" % "compile"
  )
}
```

And invoke `Angular.init()` in your `Boot` class.

```scala
package bootstrap.liftweb

class Boot {
  def boot {
    // Other stuff...
    
    net.liftmodules.ng.Angular.init()
  }
}
```

## Supported Versions

**lift-ng** is built and released to support Lift editions 2.5 and 2.6 with Scala versions 2.9.1, 2.9.1-1, 2.9.2, and 2.10; and Lift edition 3.0 with Scala version 2.10.3.  This project's scala version is purposefully set at the lowest common denominator to ensure each version compiles.

## Usage

Below are a few usage examples.  Be sure to check out the aforementioned [sample project](https://github.com/htmldoug/ng-lift-proxy) or the [test project](https://github.com/barnesjd/lift-ng/tree/master/test-project) for fully functional examples

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
<!-- The angular library -->
<script type="text/javascript" src="/scripts/angular.js"></script>

<!-- Prerequisite stuff the plugin needs -->
<script data-lift="Angular"></script>
<script src="/classpath/net/liftmodules/ng/js/liftproxy.js"></script>

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

## Scaladocs

The latest version of scaladocs are hosted thanks to [cloudbees](http://www.cloudbees.com/) continuous integration services.  There should not be any differences among the supported versions of Scala.  Nonetheless all are listed here for good measure.
* [Scala 2.10](https://liftmodules.ci.cloudbees.com/job/lift-ng/ws/target/scala-2.10/api/index.html#package)
* [Scala 2.9.2](https://liftmodules.ci.cloudbees.com/job/lift-ng/ws/target/scala-2.9.2/api/index.html#package)
* [Scala 2.9.1-1](https://liftmodules.ci.cloudbees.com/job/lift-ng/ws/target/scala-2.9.1-1/api/index.html#package)
* [Scala 2.9.1](https://liftmodules.ci.cloudbees.com/job/lift-ng/ws/target/scala-2.9.1/api/index.html#package)

## Contributing

As with any open source project, contributions are greatly appreciated.  If you find an issue or have a feature idea, we'd love to know about it!  Any of the following will help this effort tremendously.

1. Issue a Pull Request with the fix/enhancement and unit tests to validate the changes.  OR
2. Issue a Pull Request with failing tests in the [test-project](https://github.com/barnesjd/lift-ng/tree/master/test-project) to show what needs to be changed OR
3. At a minimum, [open an issue](https://github.com/barnesjd/lift-ng/issues/new) to let us know about what you've discovered.

### Pull Requests

Below is the recommended procedure for git:

1. Fork it
2. Create your feature branch (`git checkout -b my-new-feature`)
3. Commit your changes (`git commit -am 'Add some feature'`)
4. Push to the branch (`git push origin my-new-feature`)
5. Create new Pull Request

Try to include as much as you are able, such as tests, documentation, updates to this README, etc.

### Testing

Part of contributing your changes will involve testing.  The [test-project](https://github.com/barnesjd/lift-ng/tree/master/test-project) sub-directory contains and independent sbt project for thoroughly testing the **lift-ng** plugin via selenium.  At a minimum, we ask that you run the tests with your changes to ensure nothing gets inadvertently broken.  If possible, include tests which validate your fix/enhancement in any Pull Requests.

## Wish list

Here are things we would like in this library.  It's not a road map, but should at least give an idea of where we plan to explore soon.

* `AngularActor.scope.parent` support
* Optional handling for comet events received before Angular has initialized (see issue #1)
* Initial value/first resolve value for services.  The reason for providing a first value will allow the page load to deliver the values rather than require an extra round trip.
* Injection of i18n/i10n angular js file.
* Injection of ResourceBundle i18n translation.

## Change log

* *0.3.0*: Implemented support for a factory/service to return an `LAFuture[Any]`
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


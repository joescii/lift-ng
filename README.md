lift-ng
=======

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

Tutorial
========

We recommend that you view original contributor [Doug Roper](https://github.com/htmldoug)'s [youtube demo](http://www.youtube.com/watch?v=VH-S2UDN-NQ) of the functionality of this plugin.  See also the [sample project](https://github.com/htmldoug/ng-lift-proxy) as seen in the youtube video.

Configuration
=============

Add the Sonatype.org Releases repo as a resolver in your `build.sbt` or `Build.scala` as appropriate.

```scala
resolvers += "Sonatype.org Releases" at "https://oss.sonatype.org/content/repositories/releases/"
```

Add this `lift-ng` as a dependency in your `build.sbt` or `Build.scala` as appropriate.

```scala
libraryDependencies ++= {
  val liftEdition = "2.5"

  Seq(
    // Other dependencies ...
    "net.liftmodules" %% ("ng_"+liftEdition) % "0.1.1" % "compile"
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

Supported Versions
==================

**lift-ng** is built and released to support Lift edition 2.5 and Scala versions 2.9.1, 2.9.1-1, 2.9.2, and 2.10.  This project's scala version is purposefully set at the lowest common denominator to ensure each version compiles.

Usage
=====

Continuing with the sample code from the introduction, you will need a snippet which contains the definition of the angular service/factory.

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

Scaladocs
=========

The latest version of scaladocs are hosted thanks to [cloudbees](http://www.cloudbees.com/) continuous integration services.  There should not be any differences among the supported versions of Scala.  Nonetheless all are listed here for good measure.
* [Scala 2.10](https://liftmodules.ci.cloudbees.com/job/lift-ng/ws/target/scala-2.10/api/index.html#package)
* [Scala 2.9.2](https://liftmodules.ci.cloudbees.com/job/lift-ng/ws/target/scala-2.9.2/api/index.html#package)
* [Scala 2.9.1-1](https://liftmodules.ci.cloudbees.com/job/lift-ng/ws/target/scala-2.9.1-1/api/index.html#package)
* [Scala 2.9.1](https://liftmodules.ci.cloudbees.com/job/lift-ng/ws/target/scala-2.9.1/api/index.html#package)

Wish list
=========

Here are things we would like in this library.  It's not a road map, but should at least give an idea of where we plan to explore soon.

* Testing (in progress)
* Cross-Lift version support (Not currently known what editions other than 2.5 can be supported.  Will need a solution to automating the build.)
* Forward `Failure(err)` string to client on error (currently the client code always receives the string `'server error'`)
* `Future[T]` return type
* Initial value/first resolve value.  The reason for providing a first value will allow the page load to deliver the values rather than require an extra round trip.
* Injection of i18n/i10n angular js file.
* Injection of ResourceBundle i18n translation.
* Comet integration
* Actor integration
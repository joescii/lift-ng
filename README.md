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

Add this module to your `build.sbt` or `Build.scala` as appropriate.

```scala
val liftEdition = "2.5"

"net.liftmodules" %% ("ng_"+liftEdition) % "0.1.0" % "compile"
```

And invoke `Angular.init()` in your `Boot` class.

```scala
net.liftmodules.ng.Angular.init()
```

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

This `renderIfNotAlreadyDefined` returns a `scala.xml.NodeSeq`.  Hence you will need to add script tags to your target HTML page(s) for the services.  For instance

```html
<!-- The angular library -->
<script type="text/javascript" src="/scripts/angular.js"></script>

<!-- Your angular controllers, etc -->
<script type="text/javascript" src="/scripts/pony.js"></script>

<!-- Our NgPonyService snippet -->
<script data-lift="NgPonyService"></script>

<!-- Some of the other stuff the plugin needs -->
<script data-lift="Angular"></script>
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
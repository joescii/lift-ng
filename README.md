lift-ng
=======

AngularJS support for Lift

**lift-ng** is a liftmodule for easing the utilization of `AngularJS`_ in a Lift application.  The basic premise is to make it a cinch to create server back-end services for injection into your AngularJS components.  This has resulted in the creation of a Scala DSL to closely emulate the way you would define a service in JavaScript.  For a taste, compare this JavaScript...::

    angular.module('lift.pony', [])
      .factory("ponyService", function() {
        // ...
      });

... to this Scala **lift-ng**::

    angular.module("lift.pony")
      .factory("ponyService", 
      // ...
      )

    
    
    
.. _AngularJS: http://docs.angularjs.org/guide/overview
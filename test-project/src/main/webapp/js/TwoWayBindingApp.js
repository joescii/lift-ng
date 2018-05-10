angular.module('TwoWayBindingApp', ['lift-ng'])
.config(['$compileProvider', function ($compileProvider) {
  $compileProvider.debugInfoEnabled(false);
}])
.controller('Controller', function(){})
;
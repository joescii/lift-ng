angular.module('MultipleBindersApp', ['lift-ng'])
.config(['$compileProvider', function ($compileProvider) {
  $compileProvider.debugInfoEnabled(false);
}])
.controller('Controller1', function() {
})
.controller('Controller2', function() {
})
;

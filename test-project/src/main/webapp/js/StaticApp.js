angular.module('StaticApp', ['lift-ng', 'StaticServices'])
.config(['$compileProvider', function ($compileProvider) {
  $compileProvider.debugInfoEnabled(false);
}])
.controller('TestController', ['$scope', 'staticService', function($scope, svc) {
  $scope.outputStr = svc.string();
  $scope.outputInt = svc.integer();
  $scope.outputObj = svc.obj();
}]);


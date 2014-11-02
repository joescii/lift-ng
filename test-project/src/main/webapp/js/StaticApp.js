angular.module('StaticApp', ['lift-ng', 'StaticServices'])
.controller('TestController', ['$scope', 'staticService', function($scope, svc) {
  $scope.outputStr = svc.string();
  $scope.outputInt = svc.integer();
  $scope.outputObj = svc.obj();
}]);


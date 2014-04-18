angular.module('StaticApp', ['StaticServices'])
.controller('TestController', ['$scope', 'staticService', function($scope, svc) {
  $scope.outputStr = "";
  $scope.outputInt = 0;
  $scope.outputStrField = "";
  $scope.outputIntField = 0;
}]);


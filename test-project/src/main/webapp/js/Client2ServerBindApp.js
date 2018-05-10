angular.module('Client2ServerBindingApp', ['lift-ng'])
.config(['$compileProvider', function ($compileProvider) {
  $compileProvider.debugInfoEnabled(false);
}])
.controller('Controller', ['$scope', '$window', function($scope, $window) {
  $scope.input = {};
  $scope.returned = {msg:"returned"};
}]);

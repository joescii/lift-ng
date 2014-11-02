angular.module('Client2ServerBindingApp', ['lift-ng'])
.controller('Controller', ['$scope', '$window', function($scope, $window) {
  $scope.input = {};
  $scope.returned = {msg:"returned"};
}]);

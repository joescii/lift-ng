angular.module('Client2ServerBindingApp', [])
.controller('Controller', ['$scope', '$window', function($scope, $window) {
  $scope.input = {};
  $scope.returned = {msg:"returned"};
}]);

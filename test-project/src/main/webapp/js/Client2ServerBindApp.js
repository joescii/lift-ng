angular.module('Client2ServerBindingApp', [])
.controller('Controller', ['$scope', '$window', function($scope, $window) {
  $scope.inputTxt = "";
  $scope.returned = "returned";
}]);

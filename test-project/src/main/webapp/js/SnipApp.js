angular.module('SnipApp', [])
.controller('SnipController', ['$scope', function($scope) {
  $scope.output1 = "";
  $scope.clickButton1 = function() {
    $scope.output1 = $scope.input1a;
  };
  $scope.clickButton2 = function() {
    $scope.output1 = $scope.output1 + ' ' + $scope.input1b;
  };
}]);
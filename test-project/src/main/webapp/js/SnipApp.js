angular.module('SnipApp', ['SnipServices1'])
.controller('SnipController', ['$scope', '$window', 'snipServices1', function($scope, $window, snipServices1) {
  $scope.output1 = "";
  $scope.clickButton1 = function() {
    snipServices1.call1().then(
      function (str) {
        $scope.output1 = str;
      },
      function (err) {
        $window.alert("Something broke, and we don't know why");
      },
      function (progress) {
        // Not used
        $window.alert("Something REALLY broke, and we REALLY don't know why");
      }
    );
  };
  $scope.clickButton2 = function() {
    snipServices1.call2($scope.input1).then(
      function (str) {
        $scope.output1 = str;
      },
      function (err) {
        $window.alert("Something broke, and we don't know why");
      },
      function (progress) {
        // Not used
        $window.alert("Something REALLY broke, and we REALLY don't know why");
      }
    );
  };
}]);
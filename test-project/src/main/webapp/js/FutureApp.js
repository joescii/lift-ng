angular.module('FutureApp', ['Futures'])
.controller('TestController', ['$scope', 'futureServices', function($scope, svc) {
  $scope.outputA = "";
  $scope.outputB = "";

  $scope.clickButton = function() {
    svc.getFutureVal({
      str1: $scope.inputA,
      str2: $scope.inputB
    }).then(
      function (obj) {
        $scope.outputA = obj.str1;
        $scope.outputB = obj.str2;
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

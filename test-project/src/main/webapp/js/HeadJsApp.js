angular.module('HeadJsApp', [])
.controller('TestController', ['$scope', function($scope) {
  $scope.version = NET_LIFTMODULES_NG.version;
  $scope.jsPath  = NET_LIFTMODULES_NG.jsPath;
}]);


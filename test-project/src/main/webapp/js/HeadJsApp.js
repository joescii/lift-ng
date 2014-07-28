angular.module('HeadJsApp', [])
.controller('TestController', ['$scope', function($scope) {
  $scope.version = net_liftmodules_ng.version;
  $scope.jsPath  = net_liftmodules_ng.jsPath;
}]);


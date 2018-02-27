angular.module('HeadJsApp', ['lift-ng'])
.config(['$compileProvider', function ($compileProvider) {
  $compileProvider.debugInfoEnabled(false);
}])
.controller('TestController', ['$scope', function($scope) {
  $scope.version = net_liftmodules_ng.version;
  $scope.jsPath  = net_liftmodules_ng.jsPath;
}]);


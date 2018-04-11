angular.module('Server2ClientBindingApp', ['lift-ng', 'S2cBindServices'])
.config(['$compileProvider', function ($compileProvider) {
  $compileProvider.debugInfoEnabled(false);
}])
.controller('Controller', ['$scope', '$window', 'counterService', 'arrSvc', function($scope, $window, counterService, arrSvc) {
  $scope.counter = counterService;
  $scope.arrSvc  = arrSvc;
}]);

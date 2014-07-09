angular.module('Server2ClientBindingApp', ['S2cBindServices'])
.controller('Controller', ['$scope', '$window', 'counterService', 'arrSvc', function($scope, $window, counterService, arrSvc) {
  $scope.counter = counterService;
  $scope.arrSvc  = arrSvc;
}]);

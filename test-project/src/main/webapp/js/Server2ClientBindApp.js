angular.module('Server2ClientBindingApp', ['S2cBindServices'])
.controller('Controller', ['$scope', '$window', 'counterService', function($scope, $window, counterService) {
  $scope.counter = counterService;
}]);

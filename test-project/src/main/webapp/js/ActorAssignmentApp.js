angular.module('ActorAssignmentApp', ['lift-ng']).run(['$rootScope', function($rootScope) {
  $rootScope.rootStr = "Waiting...";
  $rootScope.rootObj = {
    num: "Waiting...",
    char: "Waiting..."
  };
}])
.controller('ActorController', ['$scope', function($scope) {
  $scope.scopeStr = "Waiting...";
  $scope.scopeObj = {
    num: "Waiting...",
    char: "Waiting..."
  };
}])
;

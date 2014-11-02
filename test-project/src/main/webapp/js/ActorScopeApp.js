angular.module('ActorScopeApp', ['lift-ng'])
.controller('ParentController', ['$scope', function($scope) {
  $scope.parentMsg = "Waiting...";
  $scope.$on('scope-msg', function(e, msg) {
    $scope.parentMsg = msg;
  });
}])
.controller('ActorController', ['$scope', function($scope) {
  $scope.actorMsg = "Waiting...";
  $scope.$on('scope-msg', function(e, msg) {
    $scope.actorMsg = msg;
  });
}])
.controller('ChildController', ['$scope', function($scope) {
  $scope.childMsg = "Waiting...";
  $scope.$on('scope-msg', function(e, msg) {
    $scope.childMsg = msg;
  });
}])
;

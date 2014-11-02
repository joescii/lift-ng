angular.module('ActorRootScopeApp', ['lift-ng'])
.controller('RootScopeBroadcastStringController', ['$scope', function($scope) {
  $scope.rootScopeBroadcastStringOut = "Not running";
  $scope.$on('rootScopeBroadcastString', function(e, msg) {
    $scope.rootScopeBroadcastStringOut = msg;
  });
}])
.controller('RootScopeBroadcastJsonController', ['$scope', function($scope) {
  $scope.rootScopeBroadcastJsonOut1 = "Not running";
  $scope.rootScopeBroadcastJsonOut2 = "Not running";
  $scope.$on('rootScopeBroadcastJson', function(e, obj) {
    $scope.rootScopeBroadcastJsonOut1 = obj.num;
    $scope.rootScopeBroadcastJsonOut2 = obj.char;
  });
}])
.controller('RootScopeEmitStringController', ['$rootScope', '$scope', function($rootScope, $scope) {
  $scope.rootScopeEmitStringOut = "Not running";
  $rootScope.$on('rootScopeEmitString', function(e, msg) {
    $scope.rootScopeEmitStringOut = msg;
  });
}])
.controller('RootScopeEmitJsonController', ['$rootScope', '$scope', function($rootScope, $scope) {
  $scope.rootScopeEmitJsonOut1 = "Not running";
  $scope.rootScopeEmitJsonOut2 = "Not running";
  $rootScope.$on('rootScopeEmitJson', function(e, obj) {
    $scope.rootScopeEmitJsonOut1 = obj.num;
    $scope.rootScopeEmitJsonOut2 = obj.char;
  });
}]);

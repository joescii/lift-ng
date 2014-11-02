angular.module('App1', ['lift-ng', 'Futures'])
.controller('Controller', ['$scope', 'futureServices', function($scope, svc) {
  $scope.output = "";

  $scope.click = function() {
    svc.noArg().then(function(str){
      $scope.output = str;
    });
  };
}])
;

angular.module('App2', ['Futures'])
.controller('Controller', ['$scope', 'futureServices', function($scope, svc) {
  $scope.output = "";

  $scope.click = function() {
    svc.noArg().then(function(str){
      $scope.output = str;
    });
  };
}])
;

angular.element(document).ready(function() {
  angular.bootstrap(angular.element(document.getElementById('app1')), ['App1']);
  angular.bootstrap(angular.element(document.getElementById('app2')), ['App2']);
});
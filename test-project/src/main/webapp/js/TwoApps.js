var testRun = function(appNumber) {
  return ["$rootScope", function($rootScope){
    $rootScope.$on("net_liftmodules_ng.serverCommError", function(e, count, which, request) {
      console.log("App"+appNumber+" Server communication error due to "+which+". Current count: "+count);
    });
    $rootScope.$on("net_liftmodules_ng.serverCommErrorClear", function(e, which) {
      console.log("App"+appNumber+" Server communication errors cleared after a "+which+" success!");
    });
  }];
};

angular.module('App1', ['lift-ng', 'Futures'])
.run(testRun(1))
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
.run(testRun(2))
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
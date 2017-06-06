angular.module('FailureHandlerApp', ['lift-ng', 'FailureHandler'])
.controller('MasterController', ['$scope', '$window', 'failureServices', function($scope, $window, svc) {
  var successFn = function (success) {
    $window.alert("Something broke, and we don't know why");
  };

  var failureFn = function (field) {
    return function (value) {
      $scope[field] = value;
    }
  };

  svc.defFutureAny_failure().then(successFn).catch(failureFn("defFutureAny_failure"));

  $scope.click = function() {
    svc.failure().then(
      function (success) {
        $window.alert("Something broke, and we don't know why");
      },
      function(msg){
        $scope.output = msg;
      }
    );
  };
}])
;
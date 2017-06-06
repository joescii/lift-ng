angular.module('ErrorHandlerApp', ['lift-ng', 'ErrorHandler'])
.controller('MasterController', ['$scope', '$window', 'errorServices', function($scope, $window, svc) {
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
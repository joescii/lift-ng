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

  var wireUp = function (fn, arg) {
    svc[fn](arg).then(successFn).catch(failureFn(fn));
  }

  var model = {str1: "", str2: ""};

  wireUp("defAny_failure");
  wireUp("defStringToAny_failure", "");
  wireUp("defModelToAny_failure", model);
  wireUp("defFutureAny_failure");
  wireUp("defStringToFutureAny_failure", "");
  wireUp("defModelToFutureAny_failure", model);
}])
;
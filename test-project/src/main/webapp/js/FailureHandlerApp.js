angular.module('FailureHandlerApp', ['lift-ng', 'FailureHandler'])
.controller('MasterController', ['$scope', 'failureServices', function($scope, svc) {
  var successFn = function (success) {
    console.log("Something broke, and we don't know why");
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
  wireUp("defAny_exception");
  wireUp("defStringToAny_exception", "");
  wireUp("defModelToAny_exception", model);
  wireUp("defFutureAny_outer_exception");
  wireUp("defStringToFutureAny_outer_exception", "");
  wireUp("defModelToFutureAny_outer_exception", model);
}])
;
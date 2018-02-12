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
  wireUp("defParamToAny_failure", model);
  wireUp("defFutureAny_failure");
  wireUp("defParamToFutureAny_failure", model);
  wireUp("defAny_exception");
  wireUp("defParamToAny_exception", model);
  wireUp("defFutureAny_outer_exception");
  wireUp("defParamToFutureAny_outer_exception", model);
  wireUp("defFutureAny_inner_exception");
  wireUp("defParamToFutureAny_inner_exception", model);
}])
;
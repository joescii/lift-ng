angular.module('FutureApp', ['lift-ng', 'Futures'])
.controller('NoArgController', ['$scope', 'futureServices', function($scope, svc) {
  $scope.output = "";

  $scope.click = function() {
    svc.noArg().then(function(str){
      $scope.output = str;
    });
  };
}])
.controller('FailureController', ['$scope', '$window', 'futureServices', function($scope, $window, svc) {
  $scope.output = "";

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
.controller('StringArgController', ['$scope', 'futureServices', function($scope, svc) {
  $scope.output = "";

  $scope.click = function() {
    svc.stringArg($scope.input).then(function (str) {
      $scope.output = str;
    });
  };
}])
.controller('JsonTestController', ['$scope', 'futureServices', function($scope, svc) {
  $scope.outputA = "";
  $scope.outputB = "";

  $scope.click = function() {
    svc.jsonArg({
      str1: $scope.inputA,
      str2: $scope.inputB
    }).then(
      function (obj) {
        $scope.outputA = obj.str1;
        $scope.outputB = obj.str2;
      },
      function (err) {
        $window.alert("Something broke, and we don't know why");
      },
      function (progress) {
        // Not used
        $window.alert("Something REALLY broke, and we REALLY don't know why");
      }
    );
  };
}])
.controller('EmptyResponseController', ['$scope', 'futureServices', function($scope, svc) {
  $scope.output = "";

  $scope.click = function() {
    svc.empty().then(function() {
      $scope.output = "returned";
    });
  };
}])
.controller('SatisfiedTestController', ['$scope', 'futureServices', function($scope, svc) {
  $scope.output = "";

  $scope.click = function() {
    svc.satisfied().then(function() {
      $scope.output = "satisfied";
    });
  };
}])
.controller('ScalaFutureController', ['$scope', 'futureServices', function($scope, svc) {
  $scope.output = "";

  $scope.click = function() {
    svc.scalaFuture().then(function(str){
      $scope.output = str;
    });
  };
}])
;
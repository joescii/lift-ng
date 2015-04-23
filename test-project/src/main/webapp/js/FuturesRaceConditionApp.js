angular.module('FuturesRaceConditionApp', ['lift-ng', 'FuturesRaceCondition'])
.controller('FuturesRaceConditionController', ['$scope', 'futuresRaceConditionServices', function($scope, service) {
  $scope.obj = {};

  $scope.click = function() {
    service.fetch().then(function(obj){
      for(var i = 0; i < 10; i++) {
        (function(i){obj.fs[i].then(function(f){$scope.obj[i]=f})})(i);
      }
    });
  };
}])
;
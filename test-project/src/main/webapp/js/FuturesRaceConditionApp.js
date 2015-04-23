angular.module('FuturesRaceConditionApp', ['lift-ng', 'FuturesRaceCondition'])
.controller('FuturesRaceConditionController', ['$scope', 'futuresRaceConditionServices', function($scope, service) {
  $scope.obj = {};

  $scope.click = function() {
    service.fetch().then(function(obj){
      for(var i = 0; i <= 20; i++) {
        (function(i){obj["f"+i].then(function(f){$scope.obj["f"+i]=f})})(i);
      }
    });
  };
}])
;
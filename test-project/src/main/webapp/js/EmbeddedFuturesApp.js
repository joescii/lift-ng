angular.module('EmbeddedFuturesApp', ['EmbeddedFutures'])
.config(['$parseProvider', function($parseProvider){ $parseProvider.unwrapPromises(true) }])
.controller('EmbeddedFuturesController', ['$scope', 'embeddedFutureServices', function($scope, svc) {
  $scope.obj = {};

  $scope.click = function() {
    svc.fetch().then(function(obj){
      console.log(obj);
      $scope.obj = obj;
    });
  };
}])
;
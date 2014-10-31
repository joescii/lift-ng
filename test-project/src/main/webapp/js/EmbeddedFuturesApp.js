angular.module('EmbeddedFuturesApp', ['EmbeddedFutures'])
.controller('EmbeddedFuturesController', ['$scope', 'embeddedFutureServices', function($scope, svc) {
  $scope.obj = {};

  $scope.click = function() {
    svc.fetch().then(function(obj){
      $scope.obj = obj;
    });
  };
}])
;
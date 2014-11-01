angular.module('EmbeddedFuturesApp', ['EmbeddedFutures'])
.controller('EmbeddedFuturesController', ['$scope', 'embeddedFutureServices', function($scope, svc) {
  $scope.obj = {};

  $scope.click = function() {
    var put = function(field) {
      return function(val) {
        $scope.obj[field] = val;
      }
    };

    svc.fetch().then(function(obj){
      obj.resolved.then(put("resolved"));
      obj.failed.catch(put("failed"));
    });
  };
}])
;
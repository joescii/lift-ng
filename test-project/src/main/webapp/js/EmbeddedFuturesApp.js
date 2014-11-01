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
      obj.string.then(put("string"));
      obj.obj.then(put("obj"));
      obj.arr[0].then(put("arr0"));
      obj.arr[1].then(put("arr1"));
      obj.fobj.then(function(fobj){
        fobj.resolved.then(put("fobj_resolved"));
        fobj.failed.then(put("fobj_failed"));
        fobj.string.then(put("fobj_string"));
        fobj.obj.then(put("fobj_obj"));
      });
    });
  };
}])
;
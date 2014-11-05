angular.module('EmbeddedFuturesApp', ['lift-ng', 'EmbeddedFutures'])
.controller('EmbeddedFuturesController', ['$scope', 'embeddedFutureServices', function($scope, service) {
  $scope.obj = {};
  $scope.event = {};
  $scope.binding = {};
  $scope.scala = {};

  $scope.click = function() {
    var svc = function(field) {
      return function(val) {
        $scope.obj[field] = val;
      }
    };

    service.fetch().then(function(obj){
      obj.resolved.then(svc("resolved"));
      obj.failed.catch(svc("failed"));
      obj.string.then(svc("string"));
      obj.obj.then(svc("obj"));
      obj.arr[0].then(svc("arr0"));
      obj.arr[1].then(svc("arr1"));
      obj.fobj.then(function(fobj){
        fobj.resolved.then(svc("fobj_resolved"));
        fobj.failed.catch(svc("fobj_failed"));
        fobj.string.then(svc("fobj_string"));
        fobj.obj.then(svc("fobj_obj"));
      });
    });

    var scala = function(field) {
      return function(val) {
        $scope.scala[field] = val;
      }
    };

    service.sfetch().then(function(obj){
      obj.resolved.then(scala("resolved"));
      obj.failed.catch(scala("failed"));
      obj.string.then(scala("string"));
      obj.obj.then(scala("obj"));
    });
  };

  var event = function(field) {
    return function(val) {
      $scope.event[field] = val;
    }
  };

  $scope.$on('embedded', function(e, obj){
    obj.resolved.then(event("resolved"));
    obj.failed.catch(event("failed"));
    obj.string.then(event("string"));
    obj.obj.then(event("obj"));
    obj.arr[0].then(event("arr0"));
    obj.arr[1].then(event("arr1"));
    obj.fobj.then(function(fobj){
      fobj.resolved.then(event("fobj_resolved"));
      fobj.failed.catch(event("fobj_failed"));
      fobj.string.then(event("fobj_string"));
      fobj.obj.then(event("fobj_obj"));
    });
  });

  var binding = function(field) {
    return function(val) {
      $scope.binding[field] = val;
    }
  };

  $scope.$watch('bound', function(obj){
    obj.resolved.then(binding("resolved"));
    obj.failed.catch(binding("failed"));
    obj.string.then(binding("string"));
    obj.obj.then(binding("obj"));
    obj.arr[0].then(binding("arr0"));
    obj.arr[1].then(binding("arr1"));
    obj.fobj.then(function(fobj){
      fobj.resolved.then(binding("fobj_resolved"));
      fobj.failed.catch(binding("fobj_failed"));
      fobj.string.then(binding("fobj_string"));
      fobj.obj.then(binding("fobj_obj"));
    });
  });

}])
;
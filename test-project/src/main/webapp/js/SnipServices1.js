/**
 * This is a client-side implementation of the SnipServices1.  It gets replaced before page loading via Lift.
 * The only purpose for this is to "turn off" the server and have a valid SnipServices1 implementation.
 */

angular.module('SnipServices1', [])
.factory('snipServices1', ['$q', function($q) {
  return {
    call1: function() {
      var defer;
      defer = $q.defer();
      defer.resolve('FromServer');
      return defer.promise;
    },
    call2: function(str) {
      var defer;
      defer = $q.defer();
      defer.resolve('FromServer '+str);
      return defer.promise;
    },
    callFail: function() {
      var defer = $q.defer();
      defer.reject("FAIL");
      return defer.promise;
    }
  };
}]);
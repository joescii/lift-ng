/**
 * This is a client-side implementation of the SnipServices2.  It gets replaced before page loading via Lift.
 * The only purpose for this is to "turn off" the server and have a valid SnipServices2 implementation.
 */

angular.module('SnipServices2', [])
.factory('snipServices2', ['$q', function($q) {
  return {
    call: function(obj) {
      var defer;
      defer = $q.defer();
      defer.resolve({
        str1: "FromServer client1",
        str2: "FromServer client2"
      });
      return defer.promise;
    }
  };
}]);
angular
  .module('lift-ng', [])
  .service('plumbing', [ '$q', function($q){
    var defers = {};

    var create = function(id) {
      var q = $q.defer();
      defers[id] = q;
      return q;
    };

    var resolve = function(data, q) {
      if (data.success) {
        if (typeof data.data !== 'undefined') {
          inject(data.data);
          q.resolve(data.data);
        }
        else {
          q.resolve();
        }
      } else {
        q.reject(data.msg)
      }
      return q;
    };

    // Called by the LiftNgFutureActor when a Future is fulfilled
    var fulfill = function(data, id) {
      var theId = id || data.id;
      var q = defers[theId];
      if(typeof q !== "undefined" && q !== null) { // We found our awaiting defer/promise
        resolve(data, q);
        delete defers[theId];
      } else { // We arrived before the model which embeds us!
        resolve(data, create(theId));
      }
    };

    // Called to inject promises wherever our serializer encountered a Future
    var inject = function(model) {
      for(var k in model) {
        if(model[k] == null) {
          // Don't do anything, skip
        }
        // It is a future which we need to inject
        else if(model[k]["net.liftmodules.ng.Angular.future"]) {
          var id   = model[k]["net.liftmodules.ng.Angular.future"];
          var data = model[k].data;
          var msg  = model[k].msg;
          var q = defers[id];
          if(typeof q !== "undefined" && q !== null) { // The future resolved before we arrived here
            model[k] = q.promise;
            delete defers[id];
          } else if(data) { // The future had already RESOLVED at serialization time
            var d = $q.defer();
            d.resolve(data);
            model[k] = d.promise;
          } else if(msg) { // The future had already FAILED at serialization time
            var d = $q.defer();
            d.reject(msg);
            model[k] = d.promise;
          } else { // Promise/Future pending
            model[k] = create(id).promise;
          }
        }
        // Not a future, so check children
        else if(typeof model[k] === 'object') {
          inject(model[k]);
        }
      }
    };

    return {
      createDefer: create,
      resolve: resolve,
      fulfill: fulfill,
      inject: inject
    }
  }])
  .service('liftProxy', ['$http', '$q', 'plumbing', function ($http, $q, plumbing) {
    var svc = {
      request: function (requestData) {
        var req = requestData.name+'='+encodeURIComponent(JSON.stringify({data:requestData.data}));

        var post = function() {
          return $http.post(net_liftmodules_ng.endpoint(), req, {
            headers : {
              'Content-Type': 'application/x-www-form-urlencoded; charset=UTF-8'
            }
          });
        };

        var resolve = function(response) {
          var data = response.data;

          // If there is no future ID, then we have our data and we're done.
          if(!data.futureId) {
            var defer = $q.defer();
            plumbing.resolve(data, defer);
            return defer.promise;
          }

          // Otherwise, we need to plumb out a promise
          else {
            var defer = plumbing.createDefer(data.futureId);
            return defer.promise;
          }
        };

        return post().then(resolve);
      }
    };

    return svc;
  }
]);
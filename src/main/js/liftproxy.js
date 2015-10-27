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
      if(data.msg) {
        q.reject(data.msg)
      } else {
        if (typeof data.data !== 'undefined') {
          inject(data.data);
          q.resolve(data.data);
        }
        else {
          q.resolve();
        }
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
  .service('liftProxy', ['$rootScope', '$q', 'plumbing', function ($rootScope, $q, plumbing) {
    var origCall = liftAjax.lift_actualJSONCall;
    var grabData = function(obj) {
      if(typeof obj === "object") return obj.data;
      else return obj;
    };
    liftAjax.lift_actualJSONCall = function() {
      var args = new Array(arguments.length);
      for(var i = 0; i < args.length; ++i) {
        args[i] = arguments[i];
      }
      args[0] = grabData(args[0]);
      return origCall.apply(this, args);
    };

    var svc = {
      request: function (requestData) {
        var req = {
          data: requestData.name + '=' + encodeURIComponent(JSON.stringify({data: requestData.data})),
          when: (new Date()).getTime()
        };
        console.log(req);
        console.log(req.when);
        var defer = $q.defer();

        var onSuccess = function(response) { $rootScope.$apply(function(){ // Must work under the watchful eye of angular
          // If there is no future ID, then we have our data and we're done.
          if(!response.futureId) {
            plumbing.resolve(response, defer);
          }

          // Otherwise, we need to plumb out a new promise because we'll get the value later.
          else {
            plumbing.createDefer(response.futureId).promise.then(
              function(data)  { defer.resolve(data)  },
              function(error) { defer.reject(error)  },
              function(notify){ defer.notify(notify) }
            )
          }
        })};

        var onFailure = function() { $rootScope.$apply(function() {
          defer.reject("net.liftmodules.ng.Angular.ajaxFailure");
        })};

        net_liftmodules_ng.ajax(req, onSuccess, onFailure, "json");

        return defer.promise;
      }
    };

    return svc;
  }
]);

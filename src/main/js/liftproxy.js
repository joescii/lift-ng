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
    net_liftmodules_ng.init();

    var ajaxErrorCount = 0;

    var onErrorFor = function(req) { return function() { // Currying is so elegant in JS
      $rootScope.$emit("net_liftmodules_ng.ajaxError", ++ajaxErrorCount, req);
    }};

    var toData = function (requestData) {
      return requestData.name + '=' + encodeURIComponent(JSON.stringify({data: requestData.data}))
    };
    var toEnhancedReq = function (requestData) { return {
      data: toData(requestData),
      when: (new Date()).getTime(),
      onError: onErrorFor(requestData)
    }};

    var requestFor;
    if(net_liftmodules_ng.enhancedAjax) requestFor = toEnhancedReq;
    else requestFor = toData;

    var svc = {
      request: function (requestData) {
        var req = requestFor(requestData);
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

          if(ajaxErrorCount != 0)
            $rootScope.$emit("net_liftmodules_ng.ajaxErrorClear", req);
          ajaxErrorCount = 0;
        })};

        var onFailure = function() { $rootScope.$apply(function() {
          defer.reject("net.liftmodules.ng.Angular.ajaxErrorRetryExceeded");
        })};

        net_liftmodules_ng.ajax(req, onSuccess, onFailure, "json");

        return defer.promise;
      }
    };

    return svc;
  }
]);

var net_liftmodules_ng = net_liftmodules_ng || {};
net_liftmodules_ng.init = function() { if(net_liftmodules_ng.enhancedAjax) { // Remove this condition once we can support Lift 3.x
  // We've passed {data, when} to the ajax lift machinery, so we need to pull the data part back out.
  var onlyData = function(req) {
    // This check prevents us from screwing up any non-lift-ng ajax calls someone could possibly be making.
    if(typeof req === "object") return req.data;
    else return req;
  };

  var failureWrapper = function(req, onFailure) { return function() {
    if (typeof req === "object" && typeof req.onError === "function")
      req.onError();
    onFailure(); // We know lift always passes a failure cb function
  }};

  // Wrap the json call with our hooks in place
  var origCall = liftAjax.lift_actualJSONCall;
  liftAjax.lift_actualJSONCall = function(req, onSuccess, onFailure) {
    return origCall(onlyData(req), onSuccess, failureWrapper(req, onFailure));
  };

  // Override the sort function if we should retry ajax in order.
  if(net_liftmodules_ng.retryAjaxInOrder) {
    liftAjax.lift_ajaxQueueSort = function() {
      liftAjax.lift_ajaxQueue.sort(function (a, b) {
        // If both items are one of our doctored-up requests, grab our 'when' which is the original request time.
        if(typeof a.theData === "object" && a.theData.when && typeof b.theData === "object" && b.theData.when)
          return a.theData.when - b.theData.when;
        else // Not our stuff, so let's not screw around with the original order logic.
          return a.when - b.when;
      });
    };
  }
}};
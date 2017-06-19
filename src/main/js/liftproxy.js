angular
  .module("lift-ng", [])
  .service("plumbing", [ "$q", function($q){
    var defers = {};

    var create = function(id) {
      var q = $q.defer();
      defers[id] = q;
      return q;
    };

    var resolve = function(response, q) {
      switch(response.state) {
        case "rejected":
          q.reject(response.data)
          break;
        case "resolved":
          if (typeof response.data !== "undefined") {
            inject(response.data);
            q.resolve(response.data);
          }
          else {
            q.resolve();
          }
      }
    };

    // Called by the LiftNgFutureActor when a Future is fulfilled
    var fulfill = function(response, id) {
      var q = defers[id];
      if(typeof q !== "undefined" && q !== null) { // We found our awaiting defer/promise
        resolve(response, q);
        delete defers[id];
      } else { // We arrived before the model which embeds us!
        resolve(response, create(id));
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
          var id    = model[k]["net.liftmodules.ng.Angular.future"];
          var data  = model[k].data;
          var state = model[k].state;
          var q = defers[id];
          if(typeof q !== "undefined" && q !== null) { // The future resolved before we arrived here
            model[k] = q.promise;
            delete defers[id];
          } else if(state == "resolved") { // The future had already RESOLVED at serialization time
            var d = $q.defer();
            d.resolve(data);
            model[k] = d.promise;
          } else if(state == "rejected") { // The future had already FAILED at serialization time
            var d = $q.defer();
            d.reject(data);
            model[k] = d.promise;
          } else if(state == "pending") { // Promise/Future pending
            model[k] = create(id).promise;
          }
        }
        // Not a future, so check children
        else if(typeof model[k] === "object") {
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
  .service("liftProxy", ["$rootScope", "$q", "plumbing", function ($rootScope, $q, plumbing) {
    window.net_liftmodules_ng.init();

    var onServerCommError = function(count, which, req) {
      $rootScope.$emit("net_liftmodules_ng.serverCommError", count, which, req);
    };

    var onServerCommErrorClear = function(which) {
      $rootScope.$emit("net_liftmodules_ng.serverCommErrorClear", which);
    };

    window.net_liftmodules_ng.onServerCommErrorClearCallbacks.push(onServerCommErrorClear);

    var onCometError = function(count) {
      $rootScope.$apply(function(){
        onServerCommError(count, "comet");
      });
    };

    window.net_liftmodules_ng.onServerCommErrorCallbacks.push(onCometError);

    var onErrorFor = function(req) { return function(count) {
      onServerCommError(count, "ajax", req);
    }};

    var toData = function (requestData) {
      return requestData.name + "=" + encodeURIComponent(JSON.stringify({data: requestData.data}))
    };
    var toEnhancedReq = function (requestData) { return {
      data: toData(requestData),
      when: (new Date()).getTime(),
      onError: onErrorFor(requestData)
    }};

    var requestFor;
    if(window.net_liftmodules_ng.enhancedAjax) requestFor = toEnhancedReq;
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
        })};

        var onFailure = function() { $rootScope.$apply(function() {
          defer.reject("net.liftmodules.ng.Angular.ajaxErrorRetryExceeded");
        })};

        window.net_liftmodules_ng.ajax(req, onSuccess, onFailure, "json");

        return defer.promise;
      }
    };

    return svc;
  }
]);

(function(){
  var net_liftmodules_ng = window.net_liftmodules_ng || {};
  // Careful to only init once, since it's possible to have multiple angular apps in one page
  net_liftmodules_ng.isInitialized = false;
  // Using an array of callbacks to handle case of multiple angular apps
  net_liftmodules_ng.onServerCommErrorCallbacks = [];
  net_liftmodules_ng.onServerCommErrorClearCallbacks = [];
  net_liftmodules_ng.init = function() { if(net_liftmodules_ng.enhancedAjax) { // Remove this condition once we can support Lift 3.x
    if(!net_liftmodules_ng.isInitialized) {
      var serverCommErrorCount = 0;
      var clearServerCommError = function (which) {
        if (serverCommErrorCount != 0) {
          serverCommErrorCount = 0;
          for (var i = 0; i < net_liftmodules_ng.onServerCommErrorClearCallbacks.length; i++)
            net_liftmodules_ng.onServerCommErrorClearCallbacks[i](which);
        }
      };

      if (window.liftAjax) {
        // We've passed {data, when} to the ajax lift machinery, so we need to pull the data part back out.
        var onlyData = function (req) {
          // This check prevents us from screwing up any non-lift-ng ajax calls someone could possibly be making.
          if (typeof req === "object") return req.data;
          else return req;
        };

        var ajaxFailureWrapper = function (req, onFailure) {
          return function () {
            // Note that we _could_ count all failures. However, in the unlikely event that someone is doing ajax
            // outside of lift-ng, failures can also be regular ol' Exceptions in business code which are not any
            // indication of our ability to communicate with the server via lift-ng.
            if (typeof req === "object" && typeof req.onError === "function")
              req.onError(++serverCommErrorCount);
            onFailure.apply(this, arguments); // We know lift always passes a failure cb function
          }
        };

        var ajaxSuccessWrapper = function (onSuccess) {
          return function () {
            // Although we are not counting non-lift-ng failures, we will clear any time we successfully communicate with
            // the server for any reason.
            clearServerCommError("ajax");
            onSuccess.apply(this, arguments); // We know lift always passes a success cb function
          }
        };

        // Wrap the json call with our hooks in place
        var origAjax = window.liftAjax.lift_actualJSONCall;
        window.liftAjax.lift_actualJSONCall = function (req, onSuccess, onFailure) {
          return origAjax(onlyData(req), ajaxSuccessWrapper(onSuccess), ajaxFailureWrapper(req, onFailure));
        };

        // Override the sort function if we should retry ajax in order.
        if (net_liftmodules_ng.retryAjaxInOrder) {
          window.liftAjax.lift_ajaxQueueSort = function () {
            window.liftAjax.lift_ajaxQueue.sort(function (a, b) {
              // If both items are one of our doctored-up requests, grab our 'when' which is the original request time.
              if (typeof a.theData === "object" && a.theData.when && typeof b.theData === "object" && b.theData.when)
                return a.theData.when - b.theData.when;
              else // Not our stuff, so let's not screw around with the original order logic.
                return a.when - b.when;
            });
          };
        }
      }

      if (window.liftComet) {
        var origCometOnFailure = window.liftComet.lift_handlerFailureFunc;
        window.liftComet.lift_handlerFailureFunc = function () {
          serverCommErrorCount++;
          for (var i = 0; i < net_liftmodules_ng.onServerCommErrorCallbacks.length; i++)
            net_liftmodules_ng.onServerCommErrorCallbacks[i](serverCommErrorCount);
          origCometOnFailure.apply(this, arguments);
        };

        var origCometOnSuccess = window.liftComet.lift_handlerSuccessFunc;
        window.liftComet.lift_handlerSuccessFunc = function () {
          clearServerCommError("comet");
          origCometOnSuccess.apply(this, arguments);
        };

      }

      net_liftmodules_ng.isInitialized = true;
    }
  }};

  window.net_liftmodules_ng = net_liftmodules_ng;
}).call(this);

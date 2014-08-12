angular
  .module('lift-ng', [])
  .service('liftProxy', ['$http', '$q', function ($http, $q) {
    var svc = {
      callbacks: {},
      request: function (requestData) {
        var random = function() {
          var text = "";
          var possible = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";

          for( var i=0; i < 20; i++ )
            text += possible.charAt(Math.floor(Math.random() * possible.length));

          return text;
        };

        var q = $q.defer();
        var id = random();
        var req = requestData.name+'='+encodeURIComponent(JSON.stringify({id:id, data:requestData.data}));
        var cleanup = function() {delete svc.callbacks[id];};

        var responseToQ = function(data) {
          if (data.success) {
            if (data.data) {
              q.resolve(data.data);
            }
            else {
              q.resolve();
            }
          } else {
            q.reject(data.msg)
          }
          cleanup();
        };

        svc.callbacks[id] = responseToQ;

        var returnQ = function(response) {
          var data = response.data;
          if(!data.future) {
            responseToQ(data)
          }
          return q.promise;
        };

        return $http.post(net_liftmodules_ng.contextPath + '/ajax_request/' + lift_page + '/', req, {
          headers : {
            'Content-Type': 'application/x-www-form-urlencoded; charset=UTF-8'
          }
        }).then(returnQ);
      },
      response: function(response) {
        // The callback won't exist in the case of multiple apps on one page.
        var cb = svc.callbacks[response.id];
        if(typeof cb !== "undefined" && cb !== null)
          cb(response);
      }
    };

    return svc;
  }
]);

var net_liftmodules_ng = net_liftmodules_ng || {};

;(function(){
// From https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Object/keys
  if (true) {
    Object.keys = (function () {
      'use strict';
      var hasOwnProperty = Object.prototype.hasOwnProperty,
        hasDontEnumBug = !({toString: null}).propertyIsEnumerable('toString'),
        dontEnums = [
          'toString',
          'toLocaleString',
          'valueOf',
          'hasOwnProperty',
          'isPrototypeOf',
          'propertyIsEnumerable',
          'constructor'
        ],
        dontEnumsLength = dontEnums.length;

      return function (obj) {
        if (typeof obj !== 'object' && (typeof obj !== 'function' || obj === null)) {
          throw new TypeError('Object.keys called on non-object');
        }

        var result = [], prop, i;

        for (prop in obj) {
          if (hasOwnProperty.call(obj, prop)) {
            result.push(prop);
          }
        }

        if (hasDontEnumBug) {
          for (i = 0; i < dontEnumsLength; i++) {
            if (hasOwnProperty.call(obj, dontEnums[i])) {
              result.push(dontEnums[i]);
            }
          }
        }
        return result;
      };
    }());
  }

  net_liftmodules_ng.diff = function(a, b){
    var add = {};
    var sub = {};
    var keysA = Object.keys(a);
    var keysB = Object.keys(b);

    if(b === null){
      add = a;
    }
    else if(a === null) {
      sub = a;
    }
    else {
      for (var i in keysA) {
        var k = keysA[i];
        if (typeof a[k] === "object" && typeof b[k] === "object") {
          var rec = net_liftmodules_ng.diff(a[k], b[k]);
          if (Object.keys(rec.add).length > 0) add[k] = rec.add;
          if (Object.keys(rec.sub).length > 0) sub[k] = rec.sub;
        }
        else if (a[k] !== b[k]) add[k] = a[k];
      }

      for (var i in keysB) {
        var k = keysB[i];
        if (typeof a[k] === "undefined") sub[k] = "";
      }
    }


    return {add:add, sub:sub};
  };
})();
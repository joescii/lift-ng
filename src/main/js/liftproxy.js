angular
  .module('lift-ng', [])
  .service('callbacks', function(){
    return {
      callbacks: []
    }
  })
  .service('promiseInjector', ['$q', function($q){
    return {};
  }])
  .service('liftProxy', ['$http', '$q', 'callbacks', function ($http, $q, callbacks) {
    var svc = {
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
        var cleanup = function() {delete callbacks.callbacks[id];};

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

        callbacks.callbacks[id] = responseToQ;

        var returnQ = function(response) {
          var data = response.data;
          if(!data.future) {
            responseToQ(data)
          }
          return q.promise;
        };

        return $http.post(net_liftmodules_ng.endpoint(), req, {
          headers : {
            'Content-Type': 'application/x-www-form-urlencoded; charset=UTF-8'
          }
        }).then(returnQ);
      },
      response: function(response) {
        // The callback won't exist in the case of multiple apps on one page.
        var cb = callbacks.callbacks[response.id];
        if(typeof cb !== "undefined" && cb !== null)
          cb(response);
      }
    };

    return svc;
  }
]);
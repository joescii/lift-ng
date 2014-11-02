angular
  .module('lift-ng', [])
  .service('plumbing', [ '$q', function($q){
    var defers = {};
    var random = function() {
      var text = "";
      var possible = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";

      for( var i=0; i < 20; i++ )
        text += possible.charAt(Math.floor(Math.random() * possible.length));

      return text;
    };

    var create = function(id) {
      var q = $q.defer();
      if(id) {
        defers[id] = q;
        return q;
      } else {
        var id = random();
        defers[id] = q;
        return [q, id];
      }
    };

    var fulfill = function(data, id) {
      var theId = id || data.id;
      var q = defers[theId];
      if(typeof q !== "undefined" && q !== null) {
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
        delete defers[theId];
      }
    };

    var inject = function(model) {
      for(var k in model) {
        // It is a future which we need to inject
        if(model[k]["net.liftmodules.ng.Angular.future"]) {
          var id   = model[k].id;
          var data = model[k].data;
          var msg  = model[k].msg;
          if(id) {
            model[k] = create(id).promise;
          } else if(msg) {
            var d = $q.defer();
            d.reject(msg);
            model[k] = d.promise;
          } else {
            var d = $q.defer();
            d.resolve(data);
            model[k] = d.promise;
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
      fulfill: fulfill,
      inject: inject
    }
  }])
  .service('liftProxy', ['$http', '$q', 'plumbing', function ($http, $q, plumbing) {
    var svc = {
      request: function (requestData) {
        var q = plumbing.createDefer();
        var defer = q[0];
        var id = q[1];

        // TODO: quit creating a random id and let the server generate it instead
        var req = requestData.name+'='+encodeURIComponent(JSON.stringify({id:id, data:requestData.data}));

        var post = function() {
          return $http.post(net_liftmodules_ng.endpoint(), req, {
            headers : {
              'Content-Type': 'application/x-www-form-urlencoded; charset=UTF-8'
            }
          });
        };

        var resolve = function(response) {
          var data = response.data;
          if(!data.future) {
            plumbing.fulfill(data, id)
          }
          return defer.promise;
        };

        return post().then(resolve);
      }
    };

    return svc;
  }
]);
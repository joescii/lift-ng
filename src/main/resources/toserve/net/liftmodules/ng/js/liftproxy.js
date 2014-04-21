angular
  .module('zen.lift.proxy', [])
  .factory('liftProxy', ['$http', '$q', '$rootScope', function ($http, $q, $rootScope) {
    return function (requestData) {
      var random = function() {
        var text = "";
        var possible = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";

        for( var i=0; i < 20; i++ )
          text += possible.charAt(Math.floor(Math.random() * possible.length));

        return text;
      };

      var id = random();
      var req = requestData.name+'='+encodeURIComponent(JSON.stringify({id:id, data:requestData.data}));

      var returnQ = function(response) {
        var q = $q.defer();
        var data = response.data, returnData;
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
        };
        if(data.future) {
          var cleanup = $rootScope.$on('lift-ng-future', function(e, response){
            console.log('Response!');
            console.log(response);
            responseToQ(response);
            cleanup();
          });
        } else {
          responseToQ(data)
        }
        return q.promise;
      };

      return $http.post('/ajax_request/' + lift_page + '/', req, {
        headers : {
          'Content-Type': 'application/x-www-form-urlencoded; charset=UTF-8'
        }
      }).then(returnQ);
    };
  }]);

angular
  .module('zen.lift.proxy', [])
  .factory('liftProxy', ['$http', '$q', function ($http, $q) {
    return function (requestData) {
      var onSuccess = function (response) {
        var data = response.data, returnData;
        if (data.success) {
          if (data.data) {
            returnData = data.data;
          }
        } else {
          return $q.reject(data.msg)
        }
        return returnData;
      };
      var random = function() {
        var text = "";
        var possible = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";

        for( var i=0; i < 20; i++ )
          text += possible.charAt(Math.floor(Math.random() * possible.length));

        return text;
      };
      var id = random();
      var req = requestData.name+'='+encodeURIComponent(JSON.stringify({id:id, data:requestData.data}));
      console.log(requestData);
      console.log(req);
      return $http.post('/ajax_request/' + lift_page + '/', req, {
        headers : {
          'Content-Type': 'application/x-www-form-urlencoded; charset=UTF-8'
        }
      }).then(onSuccess);
    };
  }]);

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
            return $http.post('/ajax_request/' + lift_page + '/', requestData, {
                headers : {
                    'Content-Type': 'application/x-www-form-urlencoded; charset=UTF-8'
                }
            }).then(onSuccess);
        };
    }]);

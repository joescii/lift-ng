angular.element(document).ready(function() {

  angular.module('DelayApp', ['lift-ng'])
  .controller('EarlyEmitController', ['$rootScope', '$scope', function($rootScope, $scope) {
    $scope.earlyEmitOut = ["Angular data"];
    $rootScope.$on('earlyEmit', function(e, str) {
      $scope.earlyEmitOut.push(str);
    });
  }]);

  setTimeout(function() { angular.bootstrap(document, ['DelayApp']); }, 2000);
});
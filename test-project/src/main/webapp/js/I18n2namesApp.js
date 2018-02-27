angular.module('I18nApp', ['i18n'])
.config(['$compileProvider', function ($compileProvider) {
  $compileProvider.debugInfoEnabled(false);
}])
.controller('i18nController', ['$scope', 'i18n/testBundle', 'lift', function($scope, testBundle, lift) {
  $scope.noParams = testBundle.hello;
  $scope.params   = testBundle.goodbye('Cruel World');
  $scope.other    = lift["lost.password"];
}]);
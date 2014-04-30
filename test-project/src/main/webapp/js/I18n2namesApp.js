angular.module('I18nApp', ['i18n'])
.controller('i18nController', ['$scope', 'testBundle', 'lift', function($scope, testBundle, lift) {
  $scope.noParams = testBundle.hello;
  $scope.params   = testBundle.goodbye('Cruel World');
  $scope.other    = lift["lost.password"];
}]);
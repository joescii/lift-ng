angular.module('I18nApp', ['i18n'])
.config(['$compileProvider', function ($compileProvider) {
  $compileProvider.debugInfoEnabled(false);
}])
.controller('i18nController', ['$scope', 'nonEnglish', function($scope, i18n) {
  $scope.noParams = i18n.hello;
  $scope.params   = i18n.goodbye('Cruel World');
}]);
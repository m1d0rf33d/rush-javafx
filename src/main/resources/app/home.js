//Trigger the alert listener on our backend to bind loginService
alert("__CONNECT__BACKEND__loginService");


var homeModule = angular.module('HomeModule', ['ui.router'])
.config(function($stateProvider) {

    $stateProvider
        .state('ocr-view', {
            url: '/ocr-view',
            templateUrl: 'ocr/ocr.html'
        })

})
.controller('HomeController', function($scope, $state){
    $scope.branches = [];
    angular.element(document).ready(function () {
        $scope.update();
    });

    $scope.update=function(){
       /* //This is a java service (magic motherfucker!)
        loginService.loadBranches(function(data){
            $scope.branches = data;
            $scope.$apply();
        });*/
    }

    $scope.clickTest = function() {
        $state.go('ocr-view');
        console.log($state);
    }
});
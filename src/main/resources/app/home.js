//Trigger the alert listener on our backend to bind loginService
alert("__CONNECT__BACKEND__homeService");


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
    $scope.account = {};
    angular.element(document).ready(function () {
        $scope.update();
    });

    $scope.update=function(){
       /* //This is a java service (magic motherfucker!)
        loginService.loadBranches(function(data){
            $scope.branches = data;
            $scope.$apply();
        });*/
       homeService.loadEmployeeData(function(data) {
           console.log(data);
           $scope.account = {
               id: data.name
           }
           console.log($scope.account);
           $scope.apply();
       })
    }

    $scope.clickTest = function() {
        $state.go('ocr-view');
        console.log($state);
    }
});
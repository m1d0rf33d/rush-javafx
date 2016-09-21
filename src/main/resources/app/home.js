//Trigger the alert listener on our backend to bind loginService
alert("__CONNECT__BACKEND__loginService");


var homeModule = angular.module('HomeModule', ['ui.router'])
.config(function($stateProvider) {

    $stateProvider
        .state('ocr-view', {
            url: '/ocr-view',
            template: '<div class="row">' +
            '<div class="col-sm-9 col-md-8 col-lg-6 col-lg-offset-3 col-md-offset-2 col-sm-offset-1">' +
            '<br><br>' +
            '<form action="" class="form center">' +
            '<h4 class="content-title">OCR VIEW</h4>' +
            '<input class="w100" type="text">' +
        '<label class="d-label">SELECT YOUR READER</label>' +
    '<br>' +
    '<div class="row no-padding">' +
        '<div class="col-md-5">' +
        '<input value="GET TEXT" class="w100 btn-g mb10" type="button"/>' +
        '</div>' +
        '<div class="col-md-7">' +
        '<input value="CATCH RECEIPT NO." class="w100 btn-g mb10" type="button"/>' +
        '</div>'+
        '</div>'+
        '<input value="CATCH TOTAL SALES" class="w100 btn-g mb10" type="button">'+
        '</form>'+
        '</div>'+
        '</div>'
        })

})
.controller('HomeController', function($scope, $state){
    $scope.branches = [];
    angular.element(document).ready(function () {
        $scope.update();
    });

    $scope.update=function(){
        //This is a java service (magic motherfucker!)
        loginService.loadBranches(function(data){
            $scope.branches = data;
            $scope.$apply();
        });
    }

    $scope.clickTest = function() {
        $state.go('ocr-view');
        console.log($state);
    }
});
//Trigger the alert listener on our backend to bind loginService
alert("__CONNECT__BACKEND__homeService");


var homeModule = angular.module('HomeModule', ['ui.router'])
.config(function($stateProvider) {

    $stateProvider
        .state('ocr-view', {
            url: '/ocr-view',
            templateUrl: 'ocr/ocr.html'
        })
        .state('register-view', {
            url: '/register-view',
            templateUrl: 'register.html'
        })
        .state('member-login-view', {
            url: '/member-login-view',
            templateUrl: 'member-login.html'
        })
        .state('member-profile-view', {
            url: '/member-profile-view',
            templateUrl: 'member-profile.html'
        })

})
.controller('HomeController', function($scope, $state){
    $scope.branches = [];
    $scope.account = {};
    $scope.modalMessage = "";

    $scope.memberProfle = {};

    angular.element(document).ready(function () {
        $scope.update();
    });

    $scope.update=function(){
       homeService.loadEmployeeData(function(data) {
           console.log(data);
           $scope.account = {
               id: data.name
           }
           console.log($scope.account);
         /*  $scope.apply();*/
       })
    }
    //Load view
    $scope.goToOcrVIew = function() {
        $state.go('ocr-view');
    }
    $scope.goToRegisterView = function() {
        $state.go('register-view');
    }
    $scope.goToMemberLoginView = function() {
        $state.go('member-login-view');
    }
    //register member
    $scope.register = function() {
        homeService.register(function(response) {
            if (response.error_code == '0x0') {
                $("#home-modal-message").text('Register Successful');
                //Clear fields
                $("#name").modal('show');
                $("#email").modal('show');
                $("#mobile_no").modal('show');
                $("#pin").modal('show');
            } else {
                $("#home-modal-message").text(response.message);
            }
            $("#home-modal").modal('show');
        });

    }

    //member login
    $scope.memberLogin = function() {
        homeService.memberLogin(function(response) {
            this.goToMemberProfile(response);
        });
    }
    $scope.goToMemberProfile = function(response) {
        console.log(response);
    }
});


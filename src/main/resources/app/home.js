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
    //Logged in employee data
    $scope.account = {};
    //Selected member data
    $scope.memberProfile = {};

    $scope.modalMessage = "";

    angular.element(document).ready(function () {

        $scope.update();
    });

    $scope.update=function(){
        //Load logged in employee data
       homeService.loadEmployeeData(function(data) {
           console.log(data);
           $scope.account = {
               id: data.name
           }
       })
    }
    //Load views
    $scope.goToOcrVIew = function() {
        $state.go('ocr-view');
    }
    $scope.goToRegisterView = function() {
        $state.go('register-view');
    }
    $scope.goToMemberLoginView = function() {
        $state.go('member-login-view');
    }
    $scope.goToMemberProfileView = function() {
        $state.go('member-profile-view');
    }

    //member login
    $scope.memberLogin = function() {
        console.log('memberlogin');
        $state.go('member-profile-view');
        homeService.memberLogin(function(resp) {
            $scope.memberProfile = {
                id: resp.data.id,
                name: resp.data.name
            }
        });
    }

});

function registerResponseHandler(jsonResponse) {
    var resp = JSON.parse(jsonResponse);
    if (jsonResponse.message != 'undefined') {
        //registration failed
        console.log(resp.message);
        $("#home-modal-message").text(resp.message);
    } else {
        $("#home-modal-message").text('Registration Successful.');
        //Clear fields
        $("#name").val('');
        $("#email").val('');
        $("#mobile_no").val('');
        $("#pin").val('');
    }

    $("#myModal").modal('show');
}

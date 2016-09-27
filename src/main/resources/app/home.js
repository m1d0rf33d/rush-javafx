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
        .state('give-points-view', {
            url: '/give-points-view',
            templateUrl: 'give-points.html'
        })
        .state('pay-points-view', {
            url: '/pay-points-view',
            templateUrl: 'pay-with-points.html'
        })

})
.controller('HomeController', function($scope, $state){
    //Logged in employee data
    $scope.account = {};
    //Selected member data
    $scope.memberProfile = {};

    //Prompt messages
    $scope.showModal = true;
    $scope.modalMessage = "";

    //Merchant points conversion
    $scope.pointsRule = {};
    //Default values
    $scope.pointsToEarn = 0;
    $scope.totalSales = 0;


    angular.element(document).ready(function () {
        $scope.update();
        console.log($scope.memberProfile.id);
    });

    $scope.update=function(){
        //Load logged in employee data
       homeService.loadEmployeeData(function(data) {
           console.log(data);
           $scope.account = {
               name: data.name
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

    $scope.goToGivePointsView = function() {

        if ($scope.memberProfile.id == undefined) {
            $(".alert").remove();
            $(".modal-body").prepend('<div class="alert alert-warning"> <strong>Unable to give points</strong> </div>');
            $("#home-modal-message").text('No customer is logged in.');
            $("#myModal").modal('show');
            return;
        }

        //Load merchant points conversion rules
        homeService.loadPointsRule(function(resp) {
            if (resp.message != undefined) {
                $(".alert").remove();
                $("#home-modal-message").text('Unable to retrieve points conversion. Try to reload page.');
                $(".modal-body").prepend('<div class="alert alert-warning"> <strong>Something went wrong</strong> </div>');
                $("#mymodal").modal('show');
            } else {
                $scope.pointsRule = {
                    redemption_points: resp.data.redemption_points,
                    redemption_peso: resp.data.redemption_peso,
                    earning_points: resp.data.earning_points,
                    earning_peso: resp.data.earning_peso
                }
            }

        })
        $state.go('give-points-view');
    }

    $scope.goToMemberLoginView = function() {
        $state.go('member-login-view');
    }

    //member login
    $scope.loginMember = function() {
        $scope.message = 'Searching customer information';
        $scope.memberProfile = {};

        $state.go('member-profile-view');
        homeService.loginMember(function(resp) {
            if (resp.message != undefined) {
                $scope.message = 'Customer not found';
            } else {
                $scope.memberProfile = {
                    id: resp.data.id,
                    name: resp.data.name,
                    email: resp.data.email,
                    mobile_no: resp.data.mobile_no
                }
            }
        });
    }

    $scope.givePointsToCustomer = function() {
        console.log('asd');
        //open modal
        angular.element(".alert").remove();
        angular.element("#home-modal-message").text('Loading..');
        angular.element("#myModal").modal('show');
        homeService.givePointsToCustomer();
    }

});
homeModule.controller('ModalController', function($scope, close) {

    $scope.close = function(result) {
        close(result, 500); // close, but give 500ms for bootstrap to animate
    };
});

function registerResponseHandler(jsonResponse) {
    $(".alert").remove();
    var resp = JSON.parse(jsonResponse);
    if (resp.message != undefined) {
        //registration failed
        $("#home-modal-message").text(resp.message);
        $(".modal-body").prepend('<div class="alert alert-warning"> <strong>Registration Failed</strong> </div>');
    } else {
        $(".modal-body").prepend('<div class="alert alert-success"> <strong>Registration Successful</strong> </div>');
        $("#home-modal-message").text('Customer has been registered.');
        //Clear fields
        $("#name").val('');
        $("#email").val('');
        $("#mobile_no").val('');
        $("#pin").val('');
    }

    $("#myModal").modal('show');
}

function givePointsResponseHandler(jsonResponse) {
    $(".alert").remove();
    var resp = JSON.parse(jsonResponse);
    if (resp.message != undefined) {
        //registration failed
        $("#home-modal-message").text(resp.message);
        $(".modal-body").prepend('<div class="alert alert-warning"> <strong>Give Points Failed</strong> </div>');
    } else {
        //Update available points
        homeService.getPoints();
    }

    $("#myModal").modal('show');
}

function getPointsHandler(jsonResponse) {
    var resp = JSON.parse(jsonResponse);
    $(".modal-body").prepend('<div class="alert alert-success"> <strong>Give Points Successful</strong> </div>');
    $("#home-modal-message").text('Customer earned points. Total points is ' + resp.data);
    $("#myModal").modal('show');
}

function getPointsHandler(jsonResponse) {
    var resp = JSON.parse(jsonResponse);
    $(".modal-body").prepend('<div class="alert alert-success"> <strong>Give Points Successful</strong> </div>');
    $("#home-modal-message").text('Customer earned points. Total points is ' + resp.data);
    $("#myModal").modal('show');
}
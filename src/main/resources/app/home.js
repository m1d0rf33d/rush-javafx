//Trigger the alert listener on our backend to bind loginService
alert("__CONNECT__BACKEND__homeService");


var homeModule = angular.module('HomeModule', ['ui.router','datatables','datatables.columnfilter'])
    .run(function($rootScope){
        $rootScope.memberId = undefined;
    })
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
            templateUrl: 'member-profile.html',
            controller: 'MemberProfileCtrl',
            params: {mobileNumber: null}
        })
        .state('give-points-view', {
            url: '/give-points-view',
            templateUrl: 'give-points.html'
        })
        .state('pay-points-view', {
            url: '/pay-points-view',
            templateUrl: 'pay-with-points.html'
        })
        .state('voucher-redemption-view', {
            url: '/voucher-redemption-view',
            templateUrl: 'voucher-redemption.html'
        })
        .state('issue-rewards-view', {
            url: '/issue-rewards-view',
            templateUrl: 'issue-rewards.html'
        })


})
homeModule.controller('HomeController', function($scope, $state, $rootScope, $timeout){
    //Logged in employee data
    $scope.account = {};

    //Merchant points conversion
    $scope.pointsRule = {};
    //Default values
    $scope.pointsToEarn = 0;
    $scope.totalSales = 0;

    $scope.items = [];
    $scope.allRewards = [];

    angular.element(document).ready(function () {
        $scope.update();
    });

    $scope.update=function(){
        //Load logged in employee data
       homeService.loadEmployeeData(function(data) {
           $scope.account = {
               name: data.name,
               currentDate: data.currentDate
           }
       })
    }

   //State transition bindings because a href binding is not working wtf..
    $scope.goToRegisterView = function() {
        $state.go('register-view');
    }
    $scope.goToMemberLoginView = function() {

        if ($rootScope.memberId != undefined) {
            angular.element("#home-loading-modal").modal('show');
            $timeout(function(){
                $state.go('member-profile-view');
            }, 1000);
        } else {
            $state.go('member-login-view');
        }
    }

    $scope.goToPayWithPointsView = function() {
        if ($rootScope.memberId == undefined) {
            $(".alert").remove();
            $(".home-modal-body").prepend('<div class="temp"><p>No customer is logged in</p></div>');
            $(".home-modal-body").prepend('<div class="alert alert-warning temp"> <strong>Unable to give points</strong> </div>');
            $("#myModal").modal('show');
            return;
        }
        $timeout(function(){
            $state.go('pay-points-view');
        }, 1000);

    }

    $scope.goToVoucherRedemptionView = function() {
        if ($rootScope.memberId == undefined) {
            $(".alert").remove();
            $(".home-modal-body").prepend('<div class="temp"><p>No customer is logged in</p></div>');
            $(".home-modal-body").prepend('<div class="alert alert-warning temp"> <strong>Not allowed</strong> </div>');
            $("#myModal").modal('show');
            return;
        }
        angular.element("#home-loading-modal").modal('show');
        $timeout(function(){
            $state.go('voucher-redemption-view',{},{reload:true});
        }, 1000);
    }

    $scope.goToIssueRewardsView = function() {

        if ($rootScope.memberId == undefined) {
            $(".alert").remove();
            $(".home-modal-body").prepend('<div class="temp"><p>No customer is logged in</p></div>');
            $(".home-modal-body").prepend('<div class="alert alert-warning temp"> <strong>Not allowed</strong> </div>');
            $("#myModal").modal('show');
            return;
        }
        angular.element("#home-loading-modal").modal('show');
        $timeout(function(){
            $state.go('issue-rewards-view',{},{reload:true});
        }, 1000);
    }

    $scope.logoutMember = function() {
        $rootScope.memberId = undefined;
        homeService.logoutMember();
        $state.go('member-login-view');
    }

    $scope.loadSettingsView = function() {
        homeService.loadSettingsView();
    }

    $scope.loadGivePointsView = function() {
       homeService.loadGivePointsView();
    }
    $scope.clearLoginField = function() {
        $scope.employeeId = '';
    }
    $scope.addNumber = function(num) {
        $scope.employeeId = $scope.employeeId + num;
    }
});
homeModule.directive('backImg', function(){
    return function(scope, element, attrs){
        var url = attrs.backImg;
        element.css({
            'background-image': 'url(' + url +')',
            'background-size' : 'cover'
        });
    };
});
// FUNCTIONS CALLED BY JAVA BACKEND METHODS AKA AS RESPONSEHANDLERS


function closeLoadingModal() {
    console.log('closss');
    $("#home-loading-modal").modal('hide');
}


$(document).ready(function() {
    $('#myModal').on('hidden.bs.modal', function () {
        $(".temp").remove();
    })
});
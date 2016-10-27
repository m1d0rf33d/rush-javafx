//Trigger the alert listener on our backend to bind loginService
alert("__CONNECT__BACKEND__homeService");


var homeModule = angular.module('HomeModule', ['ui.router','datatables','datatables.columnfilter','fcsa-number'])
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
        .state('transactions-view', {
            url: '/transactions-view',
            templateUrl: 'transactions.html'
        })
        .state('offline-transactions-view', {
            url: '/offline-transactions-view',
            templateUrl: 'offline-transactions.html'
        })
        .state('manual-givepoints-view', {
            url: '/manual-givepoints-view',
            templateUrl: 'manual-givepoints.html'
        }).state("otherwise", {
            url: "*path",
            templateUrl: "default.html"
        });

})
homeModule.controller('HomeController', function($scope, $state, $rootScope, $timeout){

    angular.element("#home-loading-modal").modal('show');
    setTimeout(function(){},500);

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
               currentDate: data.currentDate,
               branchLogo: data.branchLogo,
               branchName: data.branchName,
               backgroundUrl: data.backgroundUrl
           }
           angular.forEach(data.screens, function(value){
               if (value == 'REGISTER') {
                   $scope.account.registration = '1';
               }
               if (value == 'MEMBER_PROFILE') {
                   $scope.account.member_inquiry = '1';
               }
               if (value == 'GIVE_POINTS') {
                   $scope.account.give_points = '1';
               }
               if (value == 'GIVE_POINTS_OCR') {
                   $scope.account.give_points_ocr = '1';
               }
               if (value == 'PAY_WITH_POINTS') {
                   $scope.account.pay_with_points = '1';
               }
               if (value == 'REDEEM_REWARDS') {
                   $scope.account.redeem_rewards = '1';
               }
               if (value == 'ISSUE_REWARDS') {
                   $scope.account.issue_rewards = '1';
               }
               if (value == 'TRANSACTIONS_VIEW') {
                   $scope.account.transaction_view = '1';
               }
               if (value == 'OCR_SETTINGS') {
                   $scope.account.setup_ocr = '1';
               }
               if (value == 'OFFLINE_TRANSACTIONS') {
                   $scope.account.offline_transactions = '1';
               }
               if (value == 'EXIT_MEMBER') {
                   $scope.account.exit_member_details = '1';
               }
           });
       })

        homeService.fetchCustomerData(function(resp) {
            if (resp.message != undefined) {
                $scope.message = 'Customer not found';
            } else {
                $rootScope.memberId = resp.data.id;
            }
        });
    }

   //State transition bindings because a href binding is not working wtf..
    $scope.goToRegisterView = function() {
        $state.go('register-view');
        $scope.highlightButton('register');
    }
    $scope.goToMemberLoginView = function() {
        $scope.highlightButton('memberinquiry');
        if ($rootScope.memberId != undefined) {
            angular.element(".temp").remove();
            angular.element("#home-loading-modal").modal('show');
            $timeout(function(){
                $state.go('member-profile-view',{},{reload:true});
            },500);
        } else {
            $state.go('member-login-view');
        }
    }

    $scope.goToPayWithPointsView = function() {

        if ($rootScope.memberId == undefined) {
            angular.element(".temp").remove();
            $(".home-modal-body").prepend('<div class="temp"><p>No customer is logged in</p></div>');
            $(".home-modal-body").prepend('<div class="alert alert-warning temp"> <strong>Unable to give points</strong> </div>');
            $("#myModal").modal('show');
            return;
        }
        $scope.highlightButton('paywithpoints');
        angular.element("#home-loading-modal").modal('show');
        $timeout(function(){
            $state.go('pay-points-view',{},{reload:true});
        },500);

    }

    $scope.goToVoucherRedemptionView = function() {
        if ($rootScope.memberId == undefined) {
            angular.element(".temp").remove();
            $(".home-modal-body").prepend('<div class="temp"><p>Please log in customer.</p></div>');
            $(".home-modal-body").prepend('<div class="alert alert-warning temp"> <strong>Redeem Voucher</strong> </div>');
            $("#myModal").modal('show');
            return;
        }
        $scope.highlightButton('redeemrewards');
        angular.element("#home-loading-modal").modal('show');
        $timeout(function(){
            $state.go('voucher-redemption-view',{},{reload:true});
        },500);
    }

    $scope.goToIssueRewardsView = function() {

        if ($rootScope.memberId == undefined) {
            angular.element(".temp").remove();
            $(".home-modal-body").prepend('<div class="temp"><p>Please log in customer.</p></div>');
            $(".home-modal-body").prepend('<div class="alert alert-warning temp"> <strong>Issue Rewards</strong> </div>');
            $("#myModal").modal('show');
            return;
        }
        $scope.highlightButton('issuerewards');
        angular.element("#home-loading-modal").modal('show');
        $timeout(function(){
            $state.go('issue-rewards-view',{},{reload:true});
        },500);
    }

     $scope.goToTransactionsView = function() {
         if ($rootScope.memberId == undefined) {
             angular.element(".temp").remove();
             $(".home-modal-body").prepend('<div class="temp"><p>Please log in customer.</p></div>');
             $(".home-modal-body").prepend('<div class="alert alert-warning temp"> <strong>Transactions View</strong> </div>');
             $("#myModal").modal('show');
             return;
         }
         $scope.highlightButton('transactionview');
         angular.element("#home-loading-modal").modal('show');
         $timeout(function(){
             $state.go('transactions-view',{},{reload:true});
         },500);
     }
     $scope.goToOfflineTransactionsView = function() {
         $scope.highlightButton('offlinetransactions');
         angular.element("#home-loading-modal").modal('show');
         $timeout(function(){
             $state.go('offline-transactions-view',{},{reload:true});
         },500);
     }
     $scope.highlightButton = function (view) {
         angular.element('a').removeClass('selected-button');
         angular.element('li').removeClass('selected-button');
         if (view === 'register') {
             angular.element('#reg-a').addClass('selected-button');
             angular.element('#reg-li').addClass('selected-button');
         } else if (view === 'memberinquiry') {
             angular.element('#mi-a').addClass('selected-button');
             angular.element('#mi-li').addClass('selected-button');
         }  else if (view === 'givepoints') {
             angular.element('#gp-a').addClass('selected-button');
             angular.element('#gp-li').addClass('selected-button');
         }  else if (view === 'givepointsocr') {
             angular.element('#gpo-a').addClass('selected-button');
             angular.element('#gpo-li').addClass('selected-button');
         } else if (view === 'givepointsocr') {
             angular.element('#gpo-a').addClass('selected-button');
             angular.element('#gpo-li').addClass('selected-button');
         } else if (view === 'paywithpoints') {
             angular.element('#pwp-a').addClass('selected-button');
             angular.element('#pwp-li').addClass('selected-button');
         } else if (view === 'redeemrewards') {
             angular.element('#rr-a').addClass('selected-button');
             angular.element('#rr-li').addClass('selected-button');
         } else if (view === 'issuerewards') {
             angular.element('#ir-a').addClass('selected-button');
             angular.element('#ir-li').addClass('selected-button');
         } else if (view === 'transactionview') {
             angular.element('#tv-a').addClass('selected-button');
             angular.element('#tv-li').addClass('selected-button');
         } else if (view === 'offlinetransactions') {
             angular.element('#otv-a').addClass('selected-button');
             angular.element('#otv-li').addClass('selected-button');
         }
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
    $scope.loadManualGivePoints = function() {
        if ($rootScope.memberId == undefined) {
            angular.element(".temp").remove();
            $(".home-modal-body").prepend('<div class="temp"><p>Please log in customer.</p></div>');
            $(".home-modal-body").prepend('<div class="alert alert-warning temp"> <strong>Payment Details</strong> </div>');
            $("#myModal").modal('show');
            return;
        }
        $scope.highlightButton('givepoints');
        angular.element("#home-loading-modal").modal('show');
        $timeout(function(){
            $state.go('manual-givepoints-view',{},{reload:true});
        },500);
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
}).directive('myEnter', function () {
    return function (scope, element, attrs) {
        element.bind("keydown keypress", function (event) {
            if(event.which === 13) {
                scope.$apply(function (){
                    scope.$eval(attrs.myEnter);
                });

                event.preventDefault();
            }
        });
    };
}).directive('numericOnly', function(){
    return {
        require: 'ngModel',
        link: function(scope, element, attrs, modelCtrl) {

            modelCtrl.$parsers.push(function (inputValue) {
                var transformedInput = inputValue ? inputValue.replace(/[^\d.-]/g,'') : null;

                if (transformedInput!=inputValue) {
                    modelCtrl.$setViewValue(transformedInput);
                    modelCtrl.$render();
                }

                return transformedInput;
            });
        }
    };
})
    .directive('amountOnly', function(){
        return {
            require: 'ngModel',
            link: function(scope, element, attrs, modelCtrl) {

                modelCtrl.$parsers.push(function (inputValue) {
                    var transformedInput = inputValue ? inputValue.replace(/[^0-9,.]+$/g,'') : null;

                    if (transformedInput!=inputValue) {
                        modelCtrl.$setViewValue(transformedInput);
                        modelCtrl.$render();
                    }

                    return transformedInput;
                });
            }
        };
    });
// FUNCTIONS CALLED BY JAVA BACKEND METHODS AKA AS RESPONSEHANDLERS


function closeLoadingModal(resp) {
    if (resp == 'false') {
        $(".offline-mode").show();
        $(".temp").remove();
        $("#mode").text("OFFLINE");
        $(".home-modal-body").prepend('<div class="temp"><p>You are currently in offline mode, only feature available is Give Points. </p></div>');
        $(".home-modal-body").prepend('<div class="alert alert-warning temp"> <strong>Network connection error</strong> </div>');
        $("#myModal").modal('show');
    }else {
        $(".offline-mode").hide();
        $("#mode").text("");
    }
    $("#home-loading-modal").modal('hide');
}


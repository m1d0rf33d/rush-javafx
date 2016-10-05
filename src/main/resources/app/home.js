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
        .state('voucher-redemption-view', {
            url: '/voucher-redemption-view',
            templateUrl: 'voucher-redemption.html'
        })
        .state('issue-rewards-view', {
            url: '/issue-rewards-view',
            templateUrl: 'issue-rewards.html'
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
        //Load logged in customer data
        homeService.fetchCustomerData(function(resp) {
            if (resp.data != undefined) {
                $scope.memberProfile = {
                    id: resp.data.id,
                    name: resp.data.name,
                    email: resp.data.email,
                    mobile_no: resp.data.mobile_no,
                    points: resp.data.points,
                    birthdate: resp.data.birthdate,
                    gender: resp.data.gender,
                    registration_date: resp.data.registration_date
                }

                homeService.loadCustomerRewards(function(resp){
                    $scope.items = resp.data;
                });
            }
        });
    }

    //VIEWS TRANSITION
    $scope.goToOcrVIew = function() {
        $state.go('ocr-view');
    }
    $scope.goToRegisterView = function() {
        $state.go('register-view');
    }

    $scope.goToGivePointsView = function() {

        if ($scope.memberProfile.id == undefined) {
            $(".alert").remove();
            $(".home-modal-body").prepend('<div class="temp"><p>No customer is logged in</p></div>');
            $(".home-modal-body").prepend('<div class="alert alert-warning temp"> <strong>Unable to give points</strong> </div>');
            $("#myModal").modal('show');
            return;
        }

        //Load merchant points conversion rules
        homeService.loadPointsRule(function(resp) {
            if (resp.message != undefined) {
                $(".alert").remove();
                $(".home-modal-body").prepend('<div class="temp"><p>Unable to retrieve point conversion</p></div>');
                $(".home-modal-body").prepend('<div class="alert alert-warning temp"> <strong>Something went wrong</strong> </div>');
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
        if ($scope.memberProfile.id != undefined) {
            $state.go('member-profile-view');
            $scope.update();
        } else {
            $state.go('member-login-view');
        }
    }

    $scope.goToPayWithPointsView = function() {
        if ($scope.memberProfile.id == undefined) {
            $(".alert").remove();
            $(".home-modal-body").prepend('<div class="temp"><p>No customer is logged in</p></div>');
            $(".home-modal-body").prepend('<div class="alert alert-warning temp"> <strong>Unable to give points</strong> </div>');
            $("#myModal").modal('show');
            return;
        }

        $state.go('pay-points-view');
    }

    $scope.goToVoucherRedemptionView = function() {
        if ($scope.memberProfile.id == undefined) {
            $(".alert").remove();
            $(".home-modal-body").prepend('<div class="temp"><p>No customer is logged in</p></div>');
            $(".home-modal-body").prepend('<div class="alert alert-warning temp"> <strong>Not allowed</strong> </div>');
            $("#myModal").modal('show');
            return;
        }
        $scope.update();
        homeService.loadRewards(function(resp){
            $scope.allRewards = resp.data;
        });

        $state.go('voucher-redemption-view');
    }

    $scope.goToIssueRewardsView = function() {

        if ($scope.memberProfile.id == undefined) {
            $(".alert").remove();
            $(".home-modal-body").prepend('<div class="temp"><p>No customer is logged in</p></div>');
            $(".home-modal-body").prepend('<div class="alert alert-warning temp"> <strong>Not allowed</strong> </div>');
            $("#myModal").modal('show');
            return;
        }

        homeService.loadCustomerRewards(function(resp){
            console.log(resp);
            $scope.items = resp.data;
        });
        $state.go('issue-rewards-view');
    }
    // END OF VIEWS

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
                    mobile_no: resp.data.mobile_no,
                    points: resp.data.points,
                    birthdate: resp.data.birthdate,
                    gender: resp.data.gender,
                    registration_date: resp.data.registration_date
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

    $scope.logoutMember = function() {
        $scope.memberProfile = {};
        homeService.logoutMember();
        $state.go('member-login-view');
    }
    $scope.redeemRewards = function(rewardId) {
        var pin = angular.element("#"+rewardId+"_pin").val();
        homeService.redeemRewards(rewardId, pin);

        angular.element("#"+rewardId+"_pin").val('');
    }

    $scope.loadSettingsView = function() {
        homeService.loadSettingsView();
    }

    $scope.loadGivePointsView = function() {
       homeService.loadGivePointsView();
    }
    $scope.payWithPointsReset = function() {
        angular.element("#or_no").val('');
        angular.element("#amount").val('');
        angular.element("#points").val('');
    }

    $scope.issueReward = function(redeemId) {
        var pin = angular.element("#"+redeemId+"_pin").val();
        homeService.issueReward(redeemId);

        angular.element("#"+redeemId+"_pin").val('');
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

function registerResponseHandler(jsonResponse) {
    $(".alert").remove();
    var resp = JSON.parse(jsonResponse);
    if (resp.message != undefined) {
        //registration failed
        $(".home-modal-body").prepend('<div class="temp"><p>'+resp.message+'</p></div>');
        $(".home-modal-body").prepend('<div class="alert alert-warning temp"> <strong>Registration Failed</strong> </div>');
    } else {
        $(".home-modal-body").prepend('<div class="temp"><p>Customer registered.</p></div>');
        $(".home-modal-body").prepend('<div class="alert alert-success temp"> <strong>Registration Successful</strong> </div>');

        //Clear fields
        $("#name").val('');
        $("#email").val('');
        $("#mobile_no").val('');
        $("#mpin").val('');
        $("#birthdate").val('');
        $("#gender").val('-1');
    }

    $("#myModal").modal('show');
}

function getPointsHandler(jsonResponse) {
    var resp = JSON.parse(jsonResponse);
    $(".home-modal-body").prepend('<div class="temp"><p> Customer total points is '+resp.data+'</p></div>');
    $(".home-modal-body").prepend('<div class="alert alert-success temp"> <strong>Give Points Successful</strong> </div>');
    $("#myModal").modal('show');
}


function payPointsResponseHandler(jsonResponse) {
    var resp = JSON.parse(jsonResponse);
    if (resp.error_code != '0x0') {
        //Show errors messages
        if (resp.errors.or_no != undefined) {
            $(".home-modal-body").prepend('<div class="temp"><p>'+resp.errors.or_no[0]+'</p></div>');
        }
        if (resp.errors.amount != undefined) {
            $(".home-modal-body").prepend('<div class="temp"><p>'+resp.errors.amount[0]+'</p></div>');
        }
        $(".home-modal-body").prepend('<div class="alert alert-warning temp"> <strong>Pay with points failed</strong> </div>');
        $("#myModal").modal('show');
    } else {
        homeService.getPoints();
    }
}

function issueRewardsResponseHandler (jsonResponse) {
    $(".modal").modal('hide');
    $(".temp").remove();
    var resp = JSON.parse(jsonResponse);
    if (resp.error_code != '0x0') {
        $(".home-modal-body").prepend('<div class="temp"><p>'+resp.message+'</p></div>');
        $(".home-modal-body").prepend('<div class="alert alert-warning temp"> <strong>Redeem item failed</strong> </div>');
    } else {
        $("#points-span").text(resp.points);
        $(".home-modal-body").prepend('<div class="temp"><p> Redeem item successful</p></div>');
        $(".home-modal-body").prepend('<div class="alert alert-success temp"> <strong>Redeem item successful</strong> </div>');
    }
    $("#myModal").modal('show');
    location.reload();
}

function redeemRewardsResponseHandler (jsonResponse) {
    $(".modal").modal('hide');
    $(".temp").remove();
    var resp = JSON.parse(jsonResponse);
    if (resp.error_code != '0x0') {
        $(".home-modal-body").prepend('<div class="temp"><p>'+resp.message+'</p></div>');
        $(".home-modal-body").prepend('<div class="alert alert-warning temp"> <strong>Redeem item failed</strong> </div>');
    } else {
        $("#points-span").text(resp.points);
        $(".home-modal-body").prepend('<div class="temp"><p> Redeem item successful</p></div>');
        $(".home-modal-body").prepend('<div class="alert alert-success temp"> <strong>Redeem item successful</strong> </div>');
    }
    $("#myModal").modal('show');
}

function givePointsResponseHandler(jsonResponse) {
    var resp = JSON.parse(jsonResponse);
    if (resp.error_code == '0x0') {
        $("#points").val('');
        $("#or_no").val('');
        $("#amount").val('');
        $("#points-span").text(resp.points);
    }
}

$(document).ready(function() {
    $('#myModal').on('hidden.bs.modal', function () {
        $(".temp").remove();
    })
});
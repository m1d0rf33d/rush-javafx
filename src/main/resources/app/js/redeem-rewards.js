
angular.module('HomeModule')
.controller('RedeemRewardsCtrl', function($scope, $rootScope, $timeout) {
    $scope.mpin = '';
    $scope.merchantRewards = [];
    //Load merchant rewards
    homeService.loadRewards(function(resp){
        $scope.merchantRewards = resp.data;
    });

    //load customer data
    homeService.fetchCustomerData(function(resp) {
        if (resp.message != undefined) {
            $scope.message = 'Customer not found';
        } else {
            $scope.member = {
                id: resp.data.id,
                name:  resp.data.name,
                email:  resp.data.email,
                mobile_no:  resp.data.mobile_no,
                points:  resp.data.points,
                birthdate:  resp.data.birthdate,
                gender: resp.data.gender,
                registration_date:  resp.data.registration_date
            }
            $scope.activeVouchers = JSON.parse(resp.data.activeVouchers);
            $rootScope.memberId = resp.data.id;
        }
    });
    $scope.redeemRewards = function(rewardId) {
        var pin = angular.element("#"+rewardId+"_pin").val();
        angular.element("#"+rewardId+"_pin").val('');
        $scope.mpin = '';
        $("#"+rewardId).modal('hide');
        $("#"+rewardId+"_pin_modal").modal('hide');
        angular.element("#home-loading-modal").modal('show');
        $timeout(function(){
            homeService.redeemRewards(rewardId, pin);
        },500);
    }
    $scope.addNumber = function(num) {
        $scope.mpin = $scope.mpin + num;
    }
    $scope.clearLoginField = function() {
        $scope.mpin = '';
    }

});

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


angular.module('HomeModule')
.controller('RedeemRewardsCtrl', function($scope, $rootScope, $timeout) {
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
                id: resp.data.profile_id,
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
        angular.element("#"+rewardId).modal('hide');
        angular.element("#"+rewardId+"_pin_modal").modal('hide');
        angular.element("#home-loading-modal").modal('show');
        $timeout(function(){
            homeService.redeemRewards(rewardId, pin);
        },500);
    }


    $scope.addNumber  = function(num, id) {
        if ( angular.element("#"+id+"_pin").val().length < 4) {
            var r =  angular.element("#"+id+"_pin").val() + num;
            angular.element("#"+id+"_pin").val(r);
        }

    }
    $scope.clearLoginField = function(id) {
        angular.element("#"+id+"_pin").val('');
    }

    $scope.showPinModal = function(id) {
        angular.element('.pin-field').val('');
        angular.element('#' + id + '_pin_modal').modal('show');

    }

    $scope.closeModal = function(id) {
        angular.element('#' + id + '_pin_modal').modal('hide');
    }


});

function redeemRewardsResponseHandler (jsonResponse) {
    $(".modal").modal('hide');
    $(".temp").remove();
    $('#pin').val('');
    var resp = JSON.parse(jsonResponse);
    if (resp.error_code != '0x0') {
        $(".home-modal-body").prepend('<div class="temp"><p>'+resp.message+'</p></div>');
        $(".home-modal-body").prepend('<div class="alert alert-warning temp"> <strong>REDEEM REWARD</strong> </div>');
    } else {
        $("#points-span").text(resp.points);
        $(".home-modal-body").prepend('<div class="temp"><p> Redeem item successful</p></div>');
        $(".home-modal-body").prepend('<div class="alert alert-success temp"> <strong>REDEEM REWARD</strong> </div>');

    }
    $("#myModal").modal('show');
}


angular.module('HomeModule')
.controller('IssueRewardsCtrl', function($scope, $rootScope){
    $scope.activeVouchers = [];
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

    homeService.loadCustomerRewards(function(resp){
        $scope.activeVouchers = resp.data;
    });

    $scope.issueReward = function(redeemId) {
        var pin = angular.element("#"+redeemId+"_pin").val();
        angular.element("#"+redeemId+"_pin").val('');
        $(".modal").modal('hide');
        homeService.issueReward(redeemId);
    }
});

function issueRewardsResponseHandler (jsonResponse) {
    $(".temp").remove();

    var resp = JSON.parse(jsonResponse);
    if (resp.error_code != '0x0') {
        $(".home-modal-body").prepend('<div class="temp"><p>'+resp.message+'</p></div>');
        $(".home-modal-body").prepend('<div class="alert alert-warning temp"> <strong>ISSUE REWARD</strong> </div>');
    } else {
        $("#points-span").text(resp.points);
        $(".home-modal-body").prepend('<div class="temp"><p> Issue reward successful.</p></div>');
        $(".home-modal-body").prepend('<div class="alert alert-success temp"> <strong>ISSUE REWARD</strong> </div>');
        $("#"+resp.redeemId+"_div").hide();
    }
    $("#myModal").modal('show');
}
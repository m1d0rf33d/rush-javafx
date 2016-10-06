
angular.module('HomeModule')
.controller('RedeemRewardsCtrl', function($scope, $rootScope) {

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

});
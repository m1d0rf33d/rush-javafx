angular.module('HomeModule')
.controller('MemberLoginCtrl', function($scope, $state, $timeout) {
    $scope.mobile = '';
    $scope.loginMember = function() {
        angular.element("#home-loading-modal").modal('show');
        $timeout(function(){
            var mobileNumber = angular.element('#mobile_no').val();
            if(mobileNumber == '') {
                mobileNumber = 'a';
            }
            $state.go('member-profile-view', {mobileNumber: mobileNumber});
        }, 500);
    }
    $scope.addNumber = function(num) {
        if ($scope.mobile == null) {
            $scope.mobile = '';
        }
        $scope.mobile = $scope.mobile + num;
    }
    $scope.clearLoginField = function() {
        $scope.mobile = '';
    }
})
.controller('MemberProfileCtrl', function($scope, $stateParams, DTOptionsBuilder, DTColumnBuilder, $q, $rootScope) {
    console.log('entering...');
    $scope.message = "";
    var vm = this;
    $scope.activeVouchers = [];
    var getTableData = function() {
        var deferred = $q.defer();
        deferred.resolve($scope.activeVouchers);
        return deferred.promise;
    };

    vm.dtOptions = DTOptionsBuilder.fromFnPromise(getTableData).withPaginationType('full_numbers');
    vm.dtColumns = [
        DTColumnBuilder.newColumn('date').withTitle('Redemption Date'),
        DTColumnBuilder.newColumn('name').withTitle('Reward').notSortable(),
        DTColumnBuilder.newColumn('details').withTitle('Details').notSortable(),
        DTColumnBuilder.newColumn('points').withTitle('Points').notSortable()

    ];
    vm.dtInstance = {};
    vm.dtInstanceCallback = function(_dtInstance) {
        vm.dtInstance = _dtInstance;
        vm.dtInstance.reloadData(); //or something else....
        angular.element(document).find('input').focus(function() {
            homeService.showVirtualKeyboard();
        });
        angular.element(document).find('input').focusout(function() {
            homeService.hideVirtualKeyboard();
        });
    }
    if ($stateParams.mobileNumber === null) {
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
    } else {
        homeService.loginMember($stateParams.mobileNumber, function(resp) {

            if (resp.message != undefined) {
                $scope.message = 'No member found that matches the mobile number.';
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
    }
});


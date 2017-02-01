
angular.module('HomeModule')
.controller('MemberTransactionsCtrl', function($scope, DTOptionsBuilder, DTColumnBuilder, $q){
    var vm = this;
    $scope.transactions = [];
    var getTableData = function() {
        var deferred = $q.defer();
        deferred.resolve($scope.transactions);
        return deferred.promise;
    };

    vm.dtOptions = DTOptionsBuilder.fromFnPromise(getTableData).withPaginationType('full_numbers');
    vm.dtColumns = [
        DTColumnBuilder.newColumn('date').withTitle('Date'),
        DTColumnBuilder.newColumn('transaction_type').withTitle('Transaction Type'),
        DTColumnBuilder.newColumn('receipt_no').withTitle('OR Number'),
        DTColumnBuilder.newColumn('amount_paid_with_points').withTitle('Points paid'),
        DTColumnBuilder.newColumn('amount_paid_with_cash').withTitle('Cash paid'),
        DTColumnBuilder.newColumn('points_earned').withTitle('Points earned')
    ];
    vm.dtInstance = {};
    vm.dtInstanceCallback = function(_dtInstance) {
        vm.dtInstance = _dtInstance;
        vm.dtInstance.reloadData(); //or something else...
        angular.element(document).find('input').focus(function() {
           homeService.showVirtualKeyboard();
        });
        angular.element(document).find('input').focusout(function() {
            homeService.hideVirtualKeyboard();
        });
    }

    homeService.fetchCustomerData(function(resp) {
        if (resp.message != undefined) {
            $scope.message = 'Customer not found';
        } else {
            $scope.member = {
                id: resp.data.profile_id,
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

    homeService.getCustomerTransactions(function(resp){

        $scope.transactions = resp.data;
        console.log($scope.transactions);
    })
});

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
        DTColumnBuilder.newColumn('id').withTitle('Transaction ID').notSortable(),
        DTColumnBuilder.newColumn('typeStr').withTitle('Transaction Type').notSortable(),
        DTColumnBuilder.newColumn('points').withTitle('Points').notSortable(),
        DTColumnBuilder.newColumn('reward_name').withTitle('Reward').notSortable(),
        DTColumnBuilder.newColumn('total_amount').withTitle('Total amount').notSortable(),
        DTColumnBuilder.newColumn('date').withTitle('Transaction Date').notSortable()
    ];
    vm.dtInstance = {};
    vm.dtInstanceCallback = function(_dtInstance) {
        vm.dtInstance = _dtInstance;
        vm.dtInstance.reloadData(); //or something else....
    }

    homeService.fetchCustomerData(function(resp) {
        if (resp.message != undefined) {
            $scope.message = 'Customer not found';
        } else {
            $scope.member = {
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

    homeService.getCustomerTransactions(function(resp){
        $scope.transactions = resp.data;
    })
});
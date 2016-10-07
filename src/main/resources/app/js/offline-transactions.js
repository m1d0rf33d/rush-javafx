
angular.module('HomeModule')
.controller('OfflineTransactionsCtrl', function($scope, DTOptionsBuilder, DTColumnBuilder, $q) {
    var vm = this;
    $scope.offlineTransactions = [];
    var getTableData = function() {
        var deferred = $q.defer();
        deferred.resolve($scope.offlineTransactions);
        return deferred.promise;
    };

    vm.dtOptions = DTOptionsBuilder.fromFnPromise(getTableData).withPaginationType('full_numbers');
    vm.dtColumns = [
        DTColumnBuilder.newColumn('mobileNumber').withTitle('Mobile Number').notSortable(),
        DTColumnBuilder.newColumn('totalAmount').withTitle('Total Amount').notSortable(),
        DTColumnBuilder.newColumn('orNumber').withTitle('OR Number').notSortable()
    ];
    vm.dtInstance = {};
    vm.dtInstanceCallback = function(_dtInstance) {
        vm.dtInstance = _dtInstance;
        vm.dtInstance.reloadData(); //or something else....
    }
    homeService.getOfflineTransactions(function(resp){
        $scope.offlineTransactions = resp;
    });

});
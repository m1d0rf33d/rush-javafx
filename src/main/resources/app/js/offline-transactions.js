
angular.module('HomeModule')
.controller('OfflineTransactionsCtrl', function($scope, DTOptionsBuilder, DTColumnBuilder, $q, $timeout) {
    var vm = this;
    $scope.offlineTransactions = [];
    var getTableData = function() {
        var deferred = $q.defer();
        deferred.resolve($scope.offlineTransactions);
        return deferred.promise;
    };

    vm.dtOptions = DTOptionsBuilder.fromFnPromise(getTableData).withPaginationType('full_numbers');
    vm.dtColumns = [
        DTColumnBuilder.newColumn('date').withTitle('Transaction Date'),
        DTColumnBuilder.newColumn('mobileNumber').withTitle('Mobile Number').notSortable(),
        DTColumnBuilder.newColumn('totalAmount').withTitle('Total Amount').notSortable(),
        DTColumnBuilder.newColumn('orNumber').withTitle('OR Number').notSortable()
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
    homeService.getOfflineTransactions(function(resp){
        $scope.offlineTransactions = resp;
    });

    $scope.sendOfflinePoints = function() {
        angular.element("#home-loading-modal").modal('show');
        $timeout(function(){
            homeService.sendOfflinePoints();
        }, 500);
    }

});

function sendOfflinePointsResponse(resp) {
    var resp = JSON.parse(resp);
    $(".temp").remove();

    var successArray = resp.successArray;
    var htmlStr = '<div class="temp"><h4 style="font-weight:600;font-size:20px;color:#393939;">Successful transmission - '+successArray.length+' results</h4></div>';
    if (successArray.length > 0) {
        var successTable = '<table class="table table-striped temp"><tr><th>Mobile Number</th><th>Total Amount</th><th>OR Number</th><th>Date</th></tr>';
        $.each(successArray, function(index, item) {
            var tr = '<tr>' + '<td>' + item.mobileNumber + '</td>' +
                '<td>'+ item.totalAmount+'</td>' +
                '<td>'+ item.orNumber +'</td>' +
                '<td>'+ item.date +'</td></tr>';
            successTable += tr;
        });
        successTable += '</table>';
        htmlStr += successTable;
    }
    htmlStr += '<br/><br/>';

    var failedArray = resp.failedArray;
    htmlStr += '<div class="temp"><h4 style="font-weight:600;font-size:20px;color:#393939;">Failed transmission - '+failedArray.length+' results</h4></div>';
    if (failedArray.length > 0) {
        var failedTable = '<table class="table table-striped temp"><tr><th>Mobile Number</th><th>Total Amount</th><th>OR Number</th><th>Date</th><th>Message</th></tr>';
        $.each(failedArray, function(index, item) {
            var tr = '<tr><td>' + item.mobileNumber + '</td>' +
                '<td>'+ item.totalAmount+'</td>' +
                '<td>'+ item.orNumber +'</td>' +
                '<td>'+ item.date +'</td>' +
                '<td>'+ item.message +'</td></tr>';
            failedTable += tr;
        });
        failedTable += '</table>';
        htmlStr += failedTable;
    }

    $(".offline-result-body").prepend(htmlStr);
    $(".offline-result-header").prepend('<div class="alert alert-success temp"> <strong>OFFLINE TRANSACTIONS - GIVE POINTS</strong> </div>');

    var d = $("#offline-table").dataTable();
    d.fnClearTable();
    $("#offlineModal").modal('show');
}
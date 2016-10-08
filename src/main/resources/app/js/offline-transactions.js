
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
        DTColumnBuilder.newColumn('mobileNumber').withTitle('Mobile Number').notSortable(),
        DTColumnBuilder.newColumn('totalAmount').withTitle('Total Amount').notSortable(),
        DTColumnBuilder.newColumn('orNumber').withTitle('OR Number').notSortable(),
        DTColumnBuilder.newColumn('date').withTitle('Date')
    ];
    vm.dtInstance = {};
    vm.dtInstanceCallback = function(_dtInstance) {
        vm.dtInstance = _dtInstance;
        vm.dtInstance.reloadData(); //or something else....
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
    var htmlStr = '<div class="temp"><h4><b>Successful transmission - '+successArray.length+' results</b></b></h4></div>';


    var successTable = '<table class="table table-striped temp">';
    $.each(successArray, function(index, item) {
        var tr = '<tr>' + '<td>' + item.mobileNumber + '</td>' +
                '<td>'+ item.totalAmount+'</td>' +
                '<td>'+ item.orNumber +'</td>' +
                '<td>'+ item.date +'</td></tr>';
        successTable += tr;
    });
    successTable += '</table>';
    htmlStr += successTable;

    var failedArray = resp.failedArray;
    htmlStr += '<div class="temp"><h4><b>Failed transmission - '+failedArray.length+' results</b></h4></div>';
    var failedTable = '<table class="table table-striped temp">';
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
    $(".offline-result-body").prepend(htmlStr);
    $(".offline-result-header").prepend('<div class="alert alert-success temp"> <strong>SEND OFFLINE POINTS RESULT</strong> </div>');

    var d = $("#offline-table").dataTable();
    d.fnClearTable();
    $("#offlineModal").modal('show');
}
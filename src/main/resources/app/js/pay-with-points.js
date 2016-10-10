
angular.module('HomeModule')
.controller('PayWithPointsCtrl', function($scope,  $timeout) {
    $scope.showModal2 = false;
    homeService.fetchCustomerData(function(resp) {
        if (resp.data != undefined) {
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
    $scope.payWithPoints = function() {
        angular.element("#home-loading-modal").modal('show');
        $timeout(function(){
            var points = angular.element("#points").val(),
                orNumber = angular.element("#or_no").val(),
                amount = angular.element("#amount").val();
            //Call java method
            homeService.payWithPoints(points, orNumber, amount);
        },1000);

    }
    $scope.payWithPointsReset = function() {
        angular.element("#or_no").val('');
        angular.element("#amount").val('');
        angular.element("#points").val('');
    }

});

function payWithPointsResponse (jsonResponse){
    $(".temp").remove();
    angular.element("#home-loading-modal").modal('hide');
    var resp = JSON.parse(jsonResponse);
    if (resp.error_code != '0x0') {
        var errorMessage = '';
        if (resp.errors.amount != undefined) {
            errorMessage = resp.errors.amount[0];
        }
        if (resp.errors.or_no != undefined) {
            errorMessage = resp.errors.or_no[0];
        }
        if (resp.errors.points != undefined) {
            errorMessage = resp.errors.points[0];
        }
        $(".paypoints-result-body").prepend('<div class="temp"><p>'+ errorMessage+'</p></div>');
        $(".paypoints-result-body").prepend('<div class="alert alert-warning temp"> <strong>Pay with points failed.</strong> </div>');
        $("#paypoints-result-modal").modal('show');
    } else {
        $(".paypoints-result-body").prepend('<div class="temp"><p>Member points remaining '+ resp.points +'</p></div>');
        $(".paypoints-result-body").prepend('<div class="alert alert-success temp"> <strong>Pay with points successful.</strong> </div>');
        $("#paypoints-result-modal").modal('show');
        $("#points-span").text(resp.points);
    }
}
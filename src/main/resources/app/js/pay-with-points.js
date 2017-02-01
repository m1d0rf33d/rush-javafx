
angular.module('HomeModule')
.controller('PayWithPointsCtrl', function($scope,  $timeout) {
    homeService.fetchCustomerData(function(resp) {
        if (resp.data != undefined) {
            $scope.member = {
                id: resp.data.profile_id,
                name: resp.data.name,
                email: resp.data.email,
                mobile_no: resp.data.mobile_no,
                points: resp.data.points,
                birthdate: resp.data.birthdate,
                gender: resp.data.gender,
                registration_date: resp.data.registration_date,
                pointsPesoValue: resp.data.pointsPesoValue
            }

        }
    });
    $scope.payWithPoints = function() {
        angular.element("#home-loading-modal").modal('show');
        $timeout(function(){
            var points = angular.element("#points").val(),
                orNumber = angular.element("#or_no").val(),
                amount = angular.element("#amount").val(),
                pin = angular.element('#pin').val();
            angular.element('#pin').val('');
            //Call java method
            if (points == '0') {
                angular.element("#home-loading-modal").modal('hide');
                $(".temp").remove();
                $(".paypoints-result-body").prepend('<div class="temp"><p> Total points to pay cannot be 0.</p></div>');
                $(".paypoints-result-body").prepend('<div class="alert alert-warning temp"> <strong>PAY WITH POINTS</strong> </div>');
                $("#paypoints-result-modal").modal('show');
                return;
            }

            homeService.payWithPoints(points, orNumber, amount, pin);
        },1000);

    }
    $scope.payWithPointsReset = function() {
        angular.element("#or_no").val('');
        angular.element("#amount").val('');
        angular.element("#points").val('');
    }

    $scope.closeModal = function() {
        angular.element('#pin_modal').modal('hide');
        angular.element('#pin').val('');
    }
    $scope.showPinModal = function() {
        angular.element('#pin').val('');
        angular.element('#pin_modal').modal('show');
    }
});

function payWithPointsResponse (jsonResponse){
    $(".temp").remove();
    $("#home-loading-modal").modal('hide');
    $("#pin_modal").modal('hide');
    var resp = JSON.parse(jsonResponse);
    if (resp.error_code != '0x0') {
        var errorMessage = '';
        if (resp.errors != undefined) {
            if (resp.errors.amount != undefined) {
                errorMessage = resp.errors.amount[0];
            }
            if (resp.errors.or_no != undefined) {
                errorMessage = resp.errors.or_no[0];
            }
            if (resp.errors.points != undefined) {
                errorMessage = resp.errors.points[0];
            }
        }
        if (resp.message != undefined) {
            errorMessage = resp.message;
        }
        $(".paypoints-result-body").prepend('<div class="temp"><p>'+ errorMessage+'</p></div>');
        $(".paypoints-result-body").prepend('<div class="alert alert-warning temp"> <strong>PAY WITH POINTS</strong> </div>');
        $("#paypoints-result-modal").modal('show');
    } else {
        $(".paypoints-result-body").prepend('<div class="temp"><p>Member points remaining '+ resp.points +'</p></div>');
        $(".paypoints-result-body").prepend('<div class="alert alert-success temp"> <strong>PAY WITH POINTS</strong> </div>');
        $("#paypoints-result-modal").modal('show');
        $("#points-span").text(resp.points);
        $("#points-peso-span").text(resp.pointsPesoValue);
    }

    $("#or_no").val('');
    $("#amount").val('');
    $("#points").val('');
}


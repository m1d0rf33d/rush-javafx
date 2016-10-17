
angular.module('HomeModule')
.controller('GivePointsCtrl', function($scope) {
    $scope.earningPeso = '';
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
                registration_date: resp.data.registration_date
            }

        }
    });

    homeService.getPointsRule(function(resp) {
        $scope.earningPeso = resp.data.earning_peso;
    })

    $scope.givePointsManual = function() {

        var orNumber = angular.element("#or_no").val(),
            amount = angular.element("#amount").val();

        homeService.givePointsManual(orNumber, amount);
    }

    $scope.resetFields = function() {
        angular.element("#or_no").val('');
        angular.element("#points").val('0');
        angular.element("#amount").val('0');
        angular.element("#remarks").val('');
    }

});

function givePointsManualResponse(jsonResponse) {
    $(".temp").remove();
    var resp = JSON.parse(jsonResponse);
    if (resp.error_code != '0x0') {
        var message = '';
        if (resp.errors != undefined) {
            if (resp.errors.or_no != undefined) {
                message = resp.errors.or_no[0];
            }
            if (resp.errors.amount != undefined) {
                message = resp.errors.amount[0];
            }
        }

        $(".home-modal-body").prepend('<div class="temp"><p>'+message+' </p></div>');
        $(".home-modal-body").prepend('<div class="alert alert-warning temp"> <strong>Give Points failed</strong> </div>');

    } else {
        $(".home-modal-body").prepend('<div class="temp"><p> Points have been awarded to customer. </p></div>');
        $(".home-modal-body").prepend('<div class="alert alert-success temp"> <strong>Give Points successful</strong> </div>');
        $("#points-span").text(resp.points);
    }
    $("#myModal").modal('show');

    $("#or_no").val('');
    $("#amount").val('0');
    $("#points").val('0');
    $("#remarks").val('');
}
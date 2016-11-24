
angular.module('HomeModule')
    .controller('GivePointsGuestCtrl', function($scope) {
        $scope.resetFields = function() {
            angular.element("#mobile_no").val('');
            angular.element("#or_no").val('');
            angular.element("#amount").val('');
        }

        $scope.givePointsGuest = function() {

            var orNumber = angular.element("#or_no").val(),
                amount = angular.element("#amount").val(),
                mobileNo = angular.element("#mobile_no").val();

            homeService.givePointsGuest(mobileNo, orNumber, amount);
        }
    })
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
        angular.element("#points").val('');
        angular.element("#amount").val('');
        angular.element("#remarks").val('');
    }

});

function givePointsGuestResponse(jsonResponse) {
    $(".temp").remove();
    var resp = JSON.parse(jsonResponse);
    if (resp.error_code == '0x0') {
        $(".home-modal-body").prepend('<div class="temp"><p> Points has been awarded to customer. </p></div>');
        $(".home-modal-body").prepend('<div class="alert alert-success temp"> <strong>GIVE POINTS TO GUEST</strong> </div>');
        $("#or_no").val('');
        $("#amount").val('');
        $("#mobile_no").val('');
    } else {
        var message = '';
        if (resp.message != undefined) {
            message =resp.message;
        }
        $(".home-modal-body").prepend('<div class="temp"><p> '+ message +' </p></div>');
        $(".home-modal-body").prepend('<div class="alert alert-warning temp"> <strong>GIVE POINTS TO GUEST</strong> </div>');
    }
    $("#myModal").modal('show');

}

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
        if (resp.message != undefined) {
            message =resp.message;
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
    $("#amount").val('');
    $("#points").val('');
    $("#remarks").val('');
}
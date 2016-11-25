
angular.module('HomeModule')
.controller('RegisterCtrl', function($scope, $timeout){

    $scope.mobilenumber = '';
    $scope.genders = ['Male','Female'];
    $scope.register = function() {
        angular.element("#home-loading-modal").modal('show');
        $timeout(function(){
            var name = angular.element("#name").val(),
                birthdate = angular.element("#birthdate").val(),
                mpin = angular.element("#mpin").val(),
                email = angular.element("#email").val(),
                gender = $scope.gender;
                mobileNo = angular.element("#mobile_no").val();
            homeService.register(name,email,mobileNo, mpin, birthdate,gender, function(resp) {
                if (resp.error_code == '0x0') {
                    $scope.gender = '';
                }
            });
        },0);
    }
    $scope.clearAll = function() {
        angular.element("#name").val('');
        angular.element("#birthdate").val('');
        angular.element("#mpin").val('');
        angular.element("#email").val('');
        $scope.gender = '';
        angular.element("#mobile_no").val('');
    }
});

function registerResponseHandler(jsonResponse) {
    $(".temp").remove();
    var resp = JSON.parse(jsonResponse);
    if (resp.message != undefined) {
        //registration failed
        $(".home-modal-body").prepend('<div class="temp"><p>'+resp.message+'</p></div>');
        $(".home-modal-body").prepend('<div class="alert alert-warning temp"> <strong>REGISTRATION</strong> </div>');
    } else {
        $(".home-modal-body").prepend('<div class="temp"><p>Customer registered.</p></div>');
        $(".home-modal-body").prepend('<div class="alert alert-success temp"> <strong>REGISTRATION</strong> </div>');

        //Clear fields
        $("#name").val('');
        $("#email").val('');
        $("#mobile_no").val('');
        $("#mpin").val('');
        $("#birthdate").val('');
        $(".gender").prop('checked', false);
    }

    $("#myModal").modal('show');
}




angular.module('HomeModule')
.controller('RegisterCtrl', function($scope){

});

function registerResponseHandler(jsonResponse) {
    $(".alert").remove();
    var resp = JSON.parse(jsonResponse);
    if (resp.message != undefined) {
        //registration failed
        $(".home-modal-body").prepend('<div class="temp"><p>'+resp.message+'</p></div>');
        $(".home-modal-body").prepend('<div class="alert alert-warning temp"> <strong>Registration Failed</strong> </div>');
    } else {
        $(".home-modal-body").prepend('<div class="temp"><p>Customer registered.</p></div>');
        $(".home-modal-body").prepend('<div class="alert alert-success temp"> <strong>Registration Successful</strong> </div>');

        //Clear fields
        $("#name").val('');
        $("#email").val('');
        $("#mobile_no").val('');
        $("#mpin").val('');
        $("#birthdate").val('');
        $("#gender").val('-1');
    }

    $("#myModal").modal('show');
}

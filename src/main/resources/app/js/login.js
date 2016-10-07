//Trigger the alert listener on our backend to bind loginService
alert("__CONNECT__BACKEND__loginService");

//Create our angular login module
var loginModule = angular.module('LoginModule',[]);

loginModule.controller('LoginController', function($scope){
    angular.element("#login-loading-modal").modal('show');
    $scope.employeeId = '';
    $scope.branches = [];
    angular.element(document).ready(function () {
        $scope.update();
    });

   $scope.update=function(){
        //This is a java service (magic motherfucker!)
       loginService.loadBranches(function(data){
           if (data != null) {
               $scope.branches = data;
               $scope.defaultBranch = $scope.branches[0].id;
               $scope.$apply();
           }
        });
    }
    $scope.clearLoginField = function() {
        $scope.employeeId = '';
    }
    $scope.addNumber = function(num) {
        $scope.employeeId = $scope.employeeId + num;
    }
});

//Functions that are outside the angular context and will be executed by our java backend service
function loginResponseHandler(jsonResponse) {
    var resp = JSON.parse(jsonResponse);

    if (resp.error_code != '0x0') {
        $(".temp").remove();
        $(".login-modal-body").prepend('<div class="temp"><p>'+resp.message+'</p></div>');
        $(".login-modal-body").prepend('<div class="alert alert-warning temp"> <strong>Login Failed</strong> </div>');
        $("#loginModal").modal('show');
    } else {
        location.href = "home.html";
    }
}

function closeLoadingModal(resp) {
    if (resp == 'false') {
        $("#mode").text("(OFFLINE MODE)");
        $(".login-modal-body").prepend('<div class="temp"><p>You are currently in offline mode, only available feature is Give Points.</p></div>');
        $(".login-modal-body").prepend('<div class="alert alert-warning temp"> <strong>Network connection error</strong> </div>');
        $("#loginModal").modal('show');
    }
    $("#login-loading-modal").modal('hide');
}


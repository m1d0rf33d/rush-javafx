//Trigger the alert listener on our backend to bind loginService
alert("__CONNECT__BACKEND__loginService");
alert("__CONNECT__BACKEND__homeService");
//Create our angular login module
var loginModule = angular.module('LoginModule',[]);

loginModule.controller('LoginController', function($scope, $timeout){

    $scope.employeeId = '';
    $scope.branches = [];
    angular.element(document).ready(function () {
        $scope.update();
    });

   $scope.update=function(){
       angular.element("#login-loading-modal").modal('show');
       $timeout(function(){
           //This is a java service (magic motherfucker!)
           loginService.loadBranches(function(resp){
               $scope.branches = resp.data;
               $scope.defaultBranch = $scope.branches[0].id;
               $scope.$apply();
           });
       }, 500);

    }
    $scope.clearLoginField = function() {
        $scope.employeeId = '';
    }
    $scope.addNumber = function(num) {
        $scope.employeeId = $scope.employeeId + num;
    }
    $scope.login = function() {
        angular.element("#login-loading-modal").modal('show');
        var employeeId = angular.element("#employee_id").val(),
            branchId = angular.element("#branch_id").val();
        loginService.login(employeeId, branchId);
    }
});

//Functions that are outside the angular context and will be executed by our java backend service
function loginResponseHandler(jsonResponse) {
    var resp = JSON.parse(jsonResponse);
    $(".temp").remove();
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
    $(".loading-temp").remove();
    if (resp == 'false') {

        $(".login-modal-body").prepend('<div class="loading-temp"><p>You are currently in offline mode, only available feature is Give Points.</p></div>');
        $(".login-modal-body").prepend('<div class="alert alert-warning loading-temp"> <strong>Network connection error</strong> </div>');
        $("#loginModal").modal('show');
        $(".online-element").hide();
        $(".offline-element").show();
    }
    $("#login-loading-modal").modal('hide');
}


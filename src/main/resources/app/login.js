//Trigger the alert listener on our backend to bind loginService
alert("__CONNECT__BACKEND__loginService");

//Create our angular login module
var loginModule = angular.module('LoginModule',[]);

loginModule.controller('LoginController', function($scope){
    $scope.branches = [];
    angular.element(document).ready(function () {
        $scope.update();
    });

    $scope.update=function(){
        //This is a java service (magic motherfucker!)
        loginService.loadBranches(function(data){
            $scope.branches = data;
            $scope.$apply();
        });
    }
});

//Functions that are outside the angular context and will be executed by our java backend service
function loginFailed() {
    $("#login-failed").modal('show');

}

function loginSuccess() {
    location.href = "home.html#/ocr-view";
}


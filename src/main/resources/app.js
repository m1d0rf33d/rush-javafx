var loginModule = angular.module('LoginModule',[]);

loginModule.controller('LoginController', function($scope){
    $scope.branches = [];

    loginFailed = function () {
        $("#login-failed").modal('show');
    }

    // fruits
    $scope.fruits = ["loading..."];
    angular.element(document).ready(function () {
        $scope.update();
    });

    $scope.update=function(){
        $scope.fruits = ["loading..."];

        loginService.loadBranches(function(data){
            $scope.branches = data;
            $scope.$apply();
        });
    }

});

function loginFailed() {
    $("#login-failed").modal('show');
}

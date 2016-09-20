alert("__CONNECT__BACKEND__loginService");

var loginModule = angular.module('LoginModule',[]);

loginModule.controller('LoginController', function($scope){
    $scope.branches = [];


    angular.element(document).ready(function () {
        $scope.update();
    });

    $scope.update=function(){
        loginService.loadBranches(function(data){
            $scope.branches = data;
            $scope.$apply();
        });
    }

});

function testArray() {
    var arr = [];
    arr.push({id: 1, name: "test"});
    return arr;
}

function loginFailed() {
    $("#login-failed").modal('show');

}

function loginSuccess() {
    location.href = "../../ocr-view.html";
}


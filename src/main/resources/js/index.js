function loginFailed() {
    $("#login-failed").modal('show');
}

function fillBranches(branches) {
    console.log(branches.length);
    var sel = $("#login-branches");
    $.each(branches, function(key, value) {
       sel.append(($("<option></option>")
           .attr("value", value).text(key)));
    });

}
//"use strict"; // new 21/07/2014 héhé
// alert("I'm evaluated in the initial global execution context!");
//used in password_modify.xhtml

var btn = "";
var form = "";
window.onerror = function (errorMsg, url, lineNumber, column, errorObj) {
    console.info('Error: ' + errorMsg + ' Script: ' + url + ' Line: ' + lineNumber
        + ' Column: ' + column + ' StackTrace: ' +  errorObj);
    alert('Error: ' + errorMsg + ' Script: ' + url + ' Line: ' + lineNumber
        + ' Column: ' + column + ' StackTrace: ' +  errorObj);
};

function change(form) {
 try{   
    alert("entering function change()");
    alert(window.location.pathname);
    var fileName = location.href.split("/").slice(-1); 
    alert("fileName");
    alert("entering function change()" + window.location.href);
    alert("form name  = " + form.name);
    var btn = document.getElementById("form:commandpswG1");

    alert("btn 1 =" + btn);
    alert("form:commandpswG1 =" + btn);
    var btn = document.getElementById("commandpswG1".value);
    alert("btn value =" + document.getElementById("form:commandpswG1".value));
 //   alert("btn 2 =" + btn);
 //   alert("input field =" + document.getElementById("pswG1".value));
    console.info("input field =" + document.getElementById("password_modify:pswG1".value));
    if (btn.value === "Hide" ){
        console.info("input field = Hide");
        btn.value = "Hide";
        btn.innerHTML = "Close Curtain";
        document.getElementById("pswG1").innerHTML = "************";
 //       document.getElementById("result").innerHTML = topMenuChoice.value;
    }else{
        console.info("input field = Show");
        btn.value = "Show";
        btn.innerHTML = "Open Curtain";
    }
   // This is important, otherwise your form will refresh the page
        return false;
 }catch(exception){
    var message = exception.message;
        console.info("LC Exception initial variable definition = " + message);
        console.error("LC Exception initial variable definition = " + exception);
}   
    
} //end function
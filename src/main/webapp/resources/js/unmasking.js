//"use strict"; // new 21/07/2014 héhé
// alert("I'm evaluated in the initial global execution context!");
//used in password_modify.xhtml


window.onerror = function (errorMsg, url, lineNumber, column, errorObj) {
    console.info('Error: ' + errorMsg + ' Script: ' + url + ' Line: ' + lineNumber
        + ' Column: ' + column + ' StackTrace: ' +  errorObj);
    alert('Error: ' + errorMsg + ' Script: ' + url + ' Line: ' + lineNumber
        + ' Column: ' + column + ' StackTrace: ' +  errorObj);
};

function unmasking() {
 try{   
     $ = jQuery;
     var button = "";
     var field = "";
 //   alert("entering function unmasking()");
    console.info("entering function unmasking()");
    console.info("jQuery.fn.jquery = " + jQuery.fn.jquery);
///    console.info("form name = " + form.name);
    console.info("document name = " + document.name);
    console.info("URL2 = " + document.URL);
    console.info(    "There are number of forms = " + document.forms.length);
    console.info(    "The name of the first form is " + document.forms[0].name);
///    console.info(    "The name of the second form is : " + form.name + "." + document.forms[1].name);
    console.info(    "The name of the third form is " + document.forms[2].name);
    console.info(    "The name of the fourth form is " + document.forms[3].name);
 //   alert("location href = " + window.location.href);
 //   alert("after reload = " + window.location.pathname);
///    button = document.getElementById("form_password_modify:commandpswG3");
///    console.info("button 1 = " + button.value);
///    button = document.getElementById(form.name + ":commandpswG3");
 ///   console.info("button 1a = " + button.value);
 ///   field = document.getElementById("form_password_modify:pswG2".value);
 ///   console.info("field 1 = " + field);
 field = $('#form_password_modify\\:pswG2');
 console.info("field 2 = " + field.value);
 button = $('#form_password_modify\\:commandpswG3');
 console.info("button 2 = " + button.value);
 console.info("field attr type = " + field.attr.toString());
 
 var button1 = document.getElementById("form_password_modify:commandpswG3");
 console.info("button1 attribute = " + button1.getAttribute("onclick"));
 
if(field.attr('type') === 'password') {
      console.info("type = password");
   field.attr('type', 'text');
   button.removeClass('pi pi-eye-slash');
   button.addClass('pi pi-eye');
}else{
      console.info("type = text");
   field.attr('type', 'password');
   button.removeClass('pi pi-eye');
   button.addClass('pi pi-eye-slash');
}
 //   alert("exiting function unmaking");
      // This is important, otherwise your form will refresh the page
//     return false;   // why ??
 }catch(exception){
    var message = exception.message;
        console.info("LC Exception initial variable definition = " + message);
        console.error("LC Exception initial variable definition = " + exception);
}   
} //end function
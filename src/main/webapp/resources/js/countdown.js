      //https://developer.mozilla.org/en-US/docs/Web/API/Window/document
      //https://developer.mozilla.org/en-US/docs/Web/API/Document_Object_Model
      "use strict";    
      /// not used jamais pu uiliser le js externe après des heures d'essais !!
   //   alert("we start countdown.js");
      console.log("we start countdown.js");
      var seconds = 0;
      var time = "";
      var millis = 0; 
 //     var message = "";
      var now = "";
  //    var f = "";
  try{
 //  console.log("title = " + window.document.title);
 //  console.log("visible = " + window.locationbar.visible);
   console.log("URL = " + document.URL);
    console.log("entering javascript");
    time = document.getElementById('password_check:time').value;
   //    alert("password_check:time" + " = " + time);
       console.log("password_check:time = " + time);
    millis = document.getElementById('password_check:millis').value;
       alert("milis= " + millis);
       console.log("password_check:millis = " + millis);
    now = new Date().getTime();
       console.log("now  = " + now);
    if(millis > now){
        console.log("millis > now");
        seconds = (millis - now) / 1000;
        console.log("seconds calculated to = " + seconds);
    }else{
        console.log("millis < now");  //temps expiré
        alert("millis < now");  //temps expiré
        seconds = 0;
    }
        console.log("seconds to wait = " + seconds);
        alert("calculated seconds before expiration = " + seconds);

window.onerror = function (errorMsg, url, lineNumber, column, errorObj) {
    console.info('Error: ' + errorMsg + ' Script: ' + url + ' Line: ' + lineNumber
        + ' Column: ' + column + ' StackTrace: ' +  errorObj);
    alert('Error: ' + errorMsg + ' Script: ' + url + ' Line: ' + lineNumber
        + ' Column: ' + column + ' StackTrace: ' +  errorObj);
};

    function countdown() {
         seconds = seconds - 1;
         console.log("seconds remaining = " + seconds);
   //      alert("count-down seconds remaining = " + seconds);
        if (seconds < 0) { // &lt; stands for the less-than sign
            // Change your redirection link here
            alert("we redirect to login.xhtml");
            window.location = "login.xhtml"; //https://duckdev.com
            javascript_abort();
        } else {
            console.log("countdown actif = " + seconds);
            var s = Math.floor(seconds/60) + " minute(s) " + Math.floor(seconds%60) + " seconds"; 
            console.log("variable s = " + s);
    //         var x = document.getElementById('password_check:countdown');
    //           console.log("just before, x = " + x);
     //       x.innerHtML = s;
 //           console.log("just before 1");
             document.getElementById('password_check:countdown').innerHTML = s; 
  //            console.log("just after");           
            window.setTimeout("countdown()", 1000);      // Count down using javascript
        }
    }  //end function countdown
function javascript_abort(){
    console.log("from function user abort js");
   throw new Error('This is not an error. This is just to abort javascript');
} 
    // Run countdown function
 //   window.onload = countdown();
   // document.onload = countdown();
   
    countdown();
    
   }catch(exception){
    // http://www.sitepoint.com/exceptional-exception-handling-in-javascript/
  //var message = exception.message;
  // handle the exception
        console.info("LC Exception = " + exception.message);
    //    console.error("LC Exception initial variable definition = " + exception);
}  
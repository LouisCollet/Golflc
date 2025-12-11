
      console.log("we start js within .xhtml");
      var seconds = 0;
      var time = "";
      var millis = 0; 
      var now = new Date().getTime();
      var uuid = "";
   try{
 //  console.log("URL = " + document.URL);
    time = document.getElementById('password_check:time').value;
       console.log("password_check:time = " + time);
    millis = document.getElementById('password_check:millis').value;
       console.log("password_check:millis = " + millis);
       console.log("now  = " + now);
    uuid = document.getElementById('password_check:uuid').value;
    console.log("uuid = " + uuid);
 //   alert("now = " + now);
    if(millis > now) { // plus grand que
  //      console.log("millis plus grand que now");
        seconds = (millis - now) / 1000;
 //       console.log("seconds calculated to = " + seconds);
    }else{
        console.log("millis plus petit que now");  //temps expiré
        seconds = -1;
    }
        console.log("calculated seconds to wait = " + seconds);

window.onerror = function (errorMsg, url, lineNumber, column, errorObj) {
    console.info('There is an Error: ' + errorMsg + ' in the Script: ' + url + ' at Line: ' + lineNumber
        + ' at Column: ' + column + ' StackTrace: ' +  errorObj);
  //  alert('Error: ' + errorMsg + ' Script: ' + url + ' Line: ' + lineNumber
  //      + ' Column: ' + column + ' StackTrace: ' +  errorObj);
};

function countdown() {
        seconds = seconds - 1;
    //     console.log("seconds remaining = " + seconds);
        if(seconds < 0) { // &lt; stands for the less-than sign
            alert("You are too late : we will redirect you to login.xhtml");
            window.location = "login.xhtml";
            javascript_abort();
        }else{
            var s = Math.floor(seconds/60) + " minute(s) " + Math.floor(seconds%60) + " seconds"; 
    //        console.log("variable s = " + s);
            document.getElementById('password_check:countdown').innerHTML = s; 
            window.setTimeout("countdown()", 1000);      // Count down using javascript
        }
    }  //end function countdown
function javascript_abort(){
    console.log("from function user abort js");
 //   alert("called function javascript_abort");
   throw new Error('This is not an error. This is just to abort javascript');
} 
    // Run countdown function
    window.onload = countdown();   // document.onload = countdown();
    countdown();
   }catch(exception){
        console.error("js - LC Exception = " + exception.message);
}  
"use strict"; // new 21/07/2014 héhé
// alert("I'm evaluated in the initial global execution context!");
//used in payment.xhtml
// reste un bug connu : si in a entré 2 par exemple et qu'on le sélectionne et le remplace par 3 ... select !
var y1, y2, y3 ;
var t=0;
var item;
//var i;
var char, key;
//var isBackspace, isDelete, isZero;
var MP4 = [0,0,0,0,0,0]; // 6 si 4 joueurs, 4 si 2 joueurs
window.onerror = function (errorMsg, url, lineNumber, column, errorObj) {
    console.info('Error: ' + errorMsg + ' Script: ' + url + ' Line: ' + lineNumber
        + ' Column: ' + column + ' StackTrace: ' +  errorObj);
    alert('Error: ' + errorMsg + ' Script: ' + url + ' Line: ' + lineNumber
        + ' Column: ' + column + ' StackTrace: ' +  errorObj);
};

function totalprice(i, form, evt){
try{
console.info("entering totalprice with i = " + i);
// console.info("entering totalprice with event  = " + evt.keyCode);
//    if (evt.keyCode === 27) {
//        alert('Esc key pressed.');
//    }
console.info("form name  = " + form.name);
switch(i) {
     case 1: item = "Greenfee"; break;
     case 2: item = "Buggy";    break;
     case 3: item = "HTrolley"; break;
     case 4: item = "ETrolley"; break;
     case 5: item = "Clubs";    break;
     case 6: item = "Practice"; break;
     default:
        alert("non existent ITEM ");
 } //end switch evt.which est préférable ?
var isBackspace = evt.keyCode === 8;
var isDelete = evt.keyCode === 46;
var isZero = evt.keyCode === 48 || evt.keyCode === 96;
//    if (evt.keyCode === 8 || evt.keyCode === 46 || evt.keyCode === 48) {      // 48 c'est zéro alert('Backspace key pressed.');  //  alert('Del key pressed.')
  if (isBackspace || isDelete || isZero) {      // 48 c'est zéro alert('Backspace key pressed.');  //  alert('Del key pressed.')
        modification(i, item);
    }

if((evt.keyCode < 48 || evt.keyCode > 57) && (evt.keyCode < 97 || evt.keyCode > 105)){
  console.info("keyCode not numeric -  " + evt.keyCode);
  alert("keyCode not numeric = " + evt.keyCode);
  return;
}else{
  console.info("keyCode numeric - accepted = " + evt.keyCode);
  creation(i, item);
}
}catch(exception){
    var message = exception.message;
        console.info("LC Exception initial variable definition = " + message);
        console.error("LC Exception initial variable definition = " + exception);
}
//if (event.altKey && event.which === 81) {
    // alt-q being used
}
//
//} // end function totalprice



function tot_item(item){
 try{
   console.info("entering function tot_item with item = " + item);
y1 = parseInt(document.getElementById('form_payment:'.concat(item).concat("1")).innerHTML); // inputText
    console.info("y1, Unit Item Price = " + y1);
y2 = parseInt(document.getElementById('form_payment:'.concat(item).concat("2")).value);

    console.info("y2, Quantity entry = " + y2);
y3 = y1 * y2;
    console.info("y3, Global Item Price = " + y3);
console.info("ending function tot_item ! ");
  }catch(exception){
        var message = exception.message;
        console.info("LC Exception initial variable definition = " + message);
        console.error("LC Exception initial variable definition = " + exception);
}
} //end function tot_item

function modification(i, item){
  try{
  console.info("starting function modification with i = " + i + " item = " + item );
  document.getElementById('form_payment:'.concat(item).concat("3")).innerHTML = 0;
  t = t - MP4[i-1]; // substract global price saved in array
    console.info("TotalPrice Modified = " + t);
  SetMyStrings([{ name: "TotalPrice", value: t}]); // is Working
   //   increment({param1:'val1', param2:'val2'});
  //  SetMyStrings([{TotalPrice:t, Greenfee:99, Buggy:50}])
  MP4[i-1] = 0;  // éviter multipes récupérations
if(t < 0){
    alert("t ne peut être négatif= " + t);
    console.info("t ne peut être négatif= " + t);
    t = 0;
}
document.getElementById('form_payment:TotalPrice').innerHTML = t;
console.info("ending function modification ");
}catch(exception){
  var message = exception.message;
        console.info("LC Exception initial variable definition = " + message);
        console.error("LC Exception initial variable definition = " + exception);
  }
} // end function modification

function creation(i, item){
  try{
    console.info("starting function creation with i = " + i + " item = " + item );
  tot_item(item);
  console.info("case : Creation - y1 = " + y1);
  console.info("case : Creation - y2 = " + y2);
  console.info("case : Creation - y3 = " + y3);

  MP4[i-1] = y3; // save in array will be used later in case of modification !

 document.getElementById('form_payment:'.concat(item).concat("3")).innerHTML = y3;
 
 t = t + y3;
 console.info("TotalPrice = " + t);
 SetMyStrings([{ name: "TotalPrice", value: t}]); // transfert data to bean via xhtml
 document.getElementById('form_payment:TotalPrice').innerHTML = t;
 document.getElementById('form_payment:hiddenTotalPrice').value = t; // new 08*10-2018
 
console.info("ending function creation " );
}catch(exception){
    var message = exception.message;
        console.info("LC Exception initial variable definition = " + message);
        console.error("LC Exception initial variable definition = " + exception);
}
} // end function creation

function isNumberKey(evt){
    var charCode = (evt.which) ? evt.which : evt.keyCode;
    if (charCode > 31 && (charCode < 48 || charCode > 57))
        return false;
    return true;
}
function disableSelect(el){
    if(el.addEventListener){
        el.addEventListener("mousedown",disabler,"false");
    } else {
        el.attachEvent("onselectstart",disabler);
    }
}

function enableSelect(el){
    if(el.addEventListener){
	el.removeEventListener("mousedown",disabler,"false");
    } else {
        el.detachEvent("onselectstart",disabler);
    }
}

function disabler(e){
    if(e.preventDefault){ e.preventDefault(); }
    return false;
}

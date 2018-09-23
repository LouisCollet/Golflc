
"use strict"; // new 21/07/2014 héhé
try{
console.info("from entering the file, defining global functions"); 
    var init = 0;
    var ex = ""; // exception handler
    console.info("from entering the file, init = " + init);
 //   var MP4;
    var MP4 = mp_initArray(6,18); // 6 si 4 joueurs, 4 si 2 joueurs
      //  console.info("from entering the file, MP4 = " + MP4);
        console.info("array MP4 = \n" + MP4.join('\n') );
        var a;
        var b = "";
//   if(typeof louis === 'undefined') {
//        console.info("louis is undefined");
//    }
        var parArray; // = a.split("|");
  //          console.info("parArray = " + parArray);
     $ = jQuery;
    var totA = 0, totB = 0;
    var hole, player, n, str;
    var stop = "go on";
    var p1,p2,p3,p4, player,score, par, valid,rest,lead;
    var teamA = 0, teamB = 0, endC, R;
    var count = 0;
    var game;
    var el = "";
    var nameA = "";
    var nameB = "";
    var img = document.createElement("IMG");
    var path = document.location.pathname;
        console.info("path = " + path); 
    var dir = path.substring(path.indexOf('/', 1)+1, path.lastIndexOf('/'));
        console.info("dir = " + dir); 
    alert(location);
    console.info("windows location protocol = " + window.location.protocol); 
    console.info("windows location host = " + window.location.host); 
    console.info("windows location pathname = " + window.location.pathname); 
    console.info("URL = " + window.location.protocol + "//" + window.location.host + "/" + window.location.pathname); 
    var scripts = document.getElementsByTagName("script");
        console.info("script = " + scripts[scripts.length-1]);
    var script = document.currentScript;
        console.info("current script = " + script);
    var fullUrl = script.src;
        console.info("fullURL = " + fullUrl);
    
}catch(exception){
    // http://www.sitepoint.com/exceptional-exception-handling-in-javascript/
    var message = exception.message;

  // handle the exception
        console.info("LC Exception initial variable definition = " + message);
        console.error("LC Exception initial variable definition = " + exception);
} 

function mp_main(in_el) // used in score_matchplay.xhtml(all holes)
{ 
try{
    var el = in_el;
    console.info("mp_main : value = " + el.value + " id = "+ el.id);
    console.info(" starting javascript from matchplay.js, using strict : console " + Date.now() );
     // Add the $() function
 ///    $ = jQuery;
     //This is a well-known problem to integrate JSF and jQuery
     // – the colon “:” is reserved for jQuery selector.
     //  To use jQuery seletor to get the JSF id, you need
     //   to “escaped” the colon by placing two backslashes in front of it :
///     $(document).ready(function(){
///        $("#score_matchplay\\:MatchProgress5").html("This is JQuery version :" + $.fn.jquery);
///        });
///    console.debug("jQuery "+ (jQuery ? $.fn.jquery : "NOT") +" loaded");      
///    console.debug("jQuery UI "+ ($.ui ? $.ui.version : "NOT") +" loaded");  
  /////   repl(el);
//     var fs = require("fs");
//     console.info( "document width  = " + document.documentElement.clientWidth);
//     console.info( "document height = " + document.documentElement.clientHeight);

// la var init a été initialisée à 0 dans score_matchplay.xhtml, fonction  mp_init_js()    
 console.info(" ... entering mp_main with init = " + init);

    if(init === 0)  // ou si null ?
    {
        console.info("first hole result : init = " + init);
        a = document.getElementById("score_matchplay:hiddenParArray").value; 
            console.info("hidden variable hiddenParArray = " + a);
        parArray = a.split("|"); // créée et rempli array    
            console.info("parArray = \n" + parArray.join() );
        game = document.getElementById("score_matchplay:hiddenGame").value; 
            console.info("game = " + game);
  // new 10/01/2015          
////       for (var i=1; i<19; i++)
////       {
////        document.getElementById("score_matchplay:scoreTeam5-" + i).disabled = true; // line 5 disabled = Leading Team
////        document.getElementById("score_matchplay:scoreTeam6-" + i).disabled = true; // line 6 disabled = Score
////       }
////        document.getElementById("score_matchplay:result").disabled = true; // affiche résultat view
////         console.info("fields disabled = ");
    }else{
        console.info("coming back with scores, init = " + init);
    }
    init++;
    if(stop === "end")
    {   console.info("final result of round reached");
        alert("final result of round reached");
        throw "throw = final result of round reached - no more input !";
      return;
    }
  // console.info("first form = " + document.forms[0].name);


// step 3 : identifier input data   
    str = el.id;
        console.info("entry = " + el.id);
    n = str.lastIndexOf("y"); //id="scorePlay${j}-${i}" on cherche le y !
//        console.info("n = " + n);
    str = str.substring(n+1);
//        console.info("str = " + str);
    player = str.substring(0,1); //1ere pos only
        console.info("player = " + player);
    hole = str.substring(2); // 3e pos jusque fin donc 1 ou 2 pos (ex: 1 ou 18)
        console.info("hole = " + hole);
        
if(el.value === "xx")
{   alert("el value is - for = " + el.id);
    MP4[player][hole - 1] = "0"; //new 01/01/2015
    el.style.clear="initial";
    throw "input is  -  " + el.value;
    }

if(el.value === "")
{   alert("el value is blanco for = " + el.id);
 // a faire : remettre array à "0" puis sortir !!!!
    MP4[player][hole - 1] = "0"; //new 01/01/2015
    el.style.clear="initial";
    throw "input is  blanco = " + el.value;
    }

    if(isNaN(el.value))
    {
        console.info("error : input is NaN, value = " + el.value + " / id = " + el.id);
        console.info("player = " + player + " hole = " + hole);
        alert("error : input is NaN = " + el.value + " " + el.id);
        throw "input is NaN = " + el.value;
    }
    score = parseInt(el.value);
 //       console.info("score = " + score);
    par = parArray[parseInt(hole)- 1];
        console.info("player = " + (player+1) + " /hole = " + hole + " /score = " + score + " /par =  " + par);
        console.info("totA = " + totA + " /totB = " + totB);
        

  //  BackgroundScore(player,el);

// vérifier si le hole précédent est complété ...
// pas de contrôle si hole 1 !!

// * à vérifier : bug
if( (hole - 2 >= 0) && (MP4[4][hole-2] === 0) ) // pas de contrôle si hole 1 // 5e rangée du trou précédent ;pas de résultat
    {
        console.error("error : preceding hole(s) must be completed, hole = " + (hole-2) + " et " + MP4[4][hole-2]);
        alert("error : preceding hole(s) must be completed !!!");
        el.value = "";
        el.style.clear="initial";
        throw "preceding hole must be completed !!!";
    }
           //error

// step 3 : enregister score dans array MP4
     MP4[player][hole - 1] = parseInt(score); // mod 16/11/2014
        console.info("array MP4 after score updated = \n" + MP4.join(';\n') );
    
// step 3 : valider input : un seul et un seul résultat par équipe !!!
// à faire : remettre field à blanc après error : donne NaN !!
valid = mp_valid();
    console.info("valid = " + valid);
if(valid !== "OK") // il y a une erreur !
{
    alert("validation error = " + valid);
    console.error(valid);
    throw "validation error : " + valid;
}
    console.info("p1 -> p4 = " + p1 +p2 +p3 +p4);
// à faire : déclencher validation ?? encore nécessaire si p3 est encodé ou p4 est encodé        
// à modifier ici
   if(game === "MP_SINGLE")  // donc 2 joueurs !
   {
    if (p1 !== 0) teamA = p1;
    if (p2 !== 0) teamB = p2;
    nameA = document.getElementById("score_matchplay:teamPlay0").innerHTML;
    nameB = document.getElementById("score_matchplay:teamPlay1").innerHTML;
   }else{ // donc 4 joueurs
    if (p1 !== 0) teamA = p1;
    if (p2 !== 0) teamA = p2;
    if (p3 !== 0) teamB = p3;
    if (p4 !== 0) teamB = p4;
    nameA = document.getElementById("score_matchplay:teamPlay0").innerHTML;
    nameB = document.getElementById("score_matchplay:teamPlay2").innerHTML;
    }
// step 3 : calcul résultat du trou : équipe gagnante
  if(game === "MP_FOURSOME")  // donc 2 joueurs !
   {
       for (var i=1; i<19; i++)
       {
 //          var v1 = "score_matchplay:scorePlay1-";
 //          var v2 = v1.concat(i);
 //          alert("v2= " + v2);
        document.getElementById("score_matchplay:scorePlay1-" + i).disabled = true; // line 2 disabled
        document.getElementById("score_matchplay:scorePlay3-" + i).disabled = true; // line 4 disabled
     //   document.getElementById(v2).disabled = true;
       }
   }
// step 4
R = mp_endHole(teamA, teamB);
    if(R === "A")
        {totA++;}
    if(R === "B")
        {totB++;}
        
// step 5
endC = mp_endCourse(teamA, teamB);
 console.info("var endC = " + endC);
if(endC !== "GO")  // end of Course
{
    stop = "end"; // indicateur fin de partie
        console.info("end of Course reached with MP4= \n" + endC);
        alert("end of Course =" + endC);
//    document.getElementById("score_matchplay:MatchProgress5").innerHTML = endC; // affiche résultat view
 console.info("array MP4 for hidden field = \n" + MP4.join(';\n') );
    document.getElementById("score_matchplay:hiddenString").value = MP4.join(';'); // affiche résultat view
////    remoteChangeCommand(); 
    document.getElementById("score_matchplay:hiddenResult").value = endC; // affiche résultat view
    document.getElementById("score_matchplay:roundResult").innerHTML = endC; 
alert("end of Course MP4 = \n" + MP4.join(';\n'));
    // new 12/01/2015
//    document.getElementById("score_matchplay:result").enabled = true; // affiche résultat view
//       for (var i=1; i<19; i++) {
//        document.getElementById("score_matchplay:scoreTeam5-" + i).enabled = true; // line 5 enabled = Leading Team
//        document.getElementById("score_matchplay:scoreTeam6-" + i).enabled = true; // line 6 enabled = Score
//       }
    
}
// step 6 : background color
    mp_colorScore(par,el);
// step 7 : compléter ligne 5 et 6
    mp_leading();
 console.info("array MP4 after mp_leading = \n" + MP4.join(';\n') );
 console.info(" normal termination for hole = " + hole);
    teamA=0, teamB=0;
}
catch(ex)
{
    // http://www.sitepoint.com/exceptional-exception-handling-in-javascript/
        console.info("LC : we found an error = " + ex + " message = " + ex.message);
        console.error("LC Exception in mp_main()= " + ex);
}
// Update a particular HTML element with a new value
function updateHTML(elmId, value) {
    console.info("from updateHTML : elmId = " + elmId + " value = " + value );
 document.getElementById(elmId).innerHTML = value;
  var y = document.getElementById(elmId).innerHTML = value;
    console.info("from updateHTML : output = " + y.value);
}
} //end function mp_main
function mp_leading(){
 try{
        console.info("entering mp_leading with totA = " + totA + ", totB = " + totB + " hole = " + hole + " count = " + count);
        count++;
        console.info("count = " + count);
        
   //      b = MP4.join(';');
  //  console.error("LC : var b in mp_leading = " + b);
////    document.getElementById("score_matchplay:hiddenString").value = MP4.join(';');
////        remoteChangeCommand(); // voir score_matchplay.xhtml : <p:remoteCommand name="remoteChangeCommand" process="hiddenString"/>

     if(count === 1)  // résultat du premier team !
        {return;}
     if(count === 2)  // résultat du 2e team !
        {count = 0;}
     if(totA > totB){
        // on affiche le team qui mène, pas celui qui a gagné le hole !!!
        console.info(" totA > totB");
///	img.src = "/resources/images/eur.gif";
///        document.getElementById("score_matchplay:scoreTeam5-" + hole).appendChild(img);
///         img.alt = "A-no image";
        document.getElementById("score_matchplay:scoreTeam5-" + hole).innerHTML="A"; //working
        MP4[4][hole-1] = "A";
        document.getElementById("score_matchplay:scoreTeam6-" + hole).innerHTML=(totA-totB) + "UP";
        MP4[5][hole-1] = totA-totB;
    }else if(totB > totA){
           console.info(" totB > totA");
///           	img.src = "resources/images/usa.gif";
///                img.alt = "B-no image";
             document.getElementById("score_matchplay:scoreTeam5-" + hole).appendChild(img);
        document.getElementById("score_matchplay:scoreTeam5-" + hole).innerHTML="B";
        MP4[4][hole-1] = "B";
        document.getElementById("score_matchplay:scoreTeam6-" + hole).innerHTML=(totB-totA) + "UP";
        MP4[5][hole-1] = totB-totA;
    }else if(totA === totB){
           console.info(" totA = totB");
        document.getElementById("score_matchplay:scoreTeam5-" + hole).innerHTML="=";
        MP4[4][hole-1] = "=";
        document.getElementById("score_matchplay:scoreTeam6-" + hole).innerHTML="AS";
        MP4[5][hole-1] = "0";
    }else{
         console.error("error comparaison totA totB in mp_resultHole");
    }
 }catch(error){
    console.error("LC : we found an error in mp_leading = " + error);}
}// end function mp_leading

function mp_valid(){
try{
   if(game === "MP_SINGLE")  // donc 2 joueurs !
        {return "OK";} // pas de validations si 2 joueurs
    p1 = parseInt(MP4[0][hole - 1]);
 //       console.log("un = " + p1);
    p2 = parseInt(MP4[1][hole - 1]);
 //       console.log("deux = " + p2);
    p3 = parseInt(MP4[2][hole - 1]);
 //       console.log("trois = " + p3);
    p4 = parseInt(MP4[3][hole - 1]);
 //       console.log("quatre = " + p4);    
    if( (p1 !== 0 || isNaN(p1)) && (p2 !== 0 || isNaN(p2)) )   //résultat pour player 1 et 2 ou > 0 ??
    {
        return "Error Team A (2 results) : player 1 = " + p1 + " // player 2 = " + p2;
        el.value = ""; // new 11/01/2015
        // à faire : remettre array à "0"
    }
    if((p1 === 0) && (p2 === 0))  //résultat pour player 1 et 2 ou > 0 ??
    {
        return "Error Team A (no results) : player 1 = " + p1 + " // player 2 = " + p2;
    }
    
    if((p3 !== 0) && (p4 !== 0))  //résultat pour player 3 et 4 ou > 0 ??
    {
        return "Error Team B (2 results) : player 3 = " + p3 + " // player 4 = " + p4;
    }
    /////////////
//    if(((p1 !== 0) || (p2 !== 0)) && ((p3 === 0) && (p4 === 0)))  //résultat pour player 3 et 4 ou > 0 ??
//    {
//        return "Error Team B (no results) : player 3 = " + p3 + " // player 4 = " + p4;
//    }
return "OK";
 }catch(error){
    console.error("LC : we found an error in mp_valid = " + error);}
}// end function mp_valid

function mp_endHole(teamA, teamB){
 try{   
        console.info("entering mp_endHole with teamA = " + teamA + " and teamB = " + teamB);
        console.info("starting résultat hole = " + parseInt(hole));
    if(teamA === 0)
        {return "0";}
    if(teamB === 0)
        {return "0";}
    if(teamA < teamB){
         return "A";
            console.info("totA incremented = " + totA);
    }else if(teamB < teamA){
        return "B";
            console.info("totB incremented = " + totB);
     }else{
        return "S";
        console.info("hole square : totA = totB");
    }
 }catch(ex){
    console.error("LC : we found an error in mp_endHole = " + ex);
    throw new Error("Boo! " + ex.message);}
}// end function mp_endHole

function mp_endCourse(teamA, teamB){
 try{
        console.info("entering mp_endCourse with teamA = " + teamA + " and teamB = " + teamB + " nameA = " + nameA + " nameB = " + nameB);
        console.info("starting résultat partie with hole = " + parseInt(hole));
    rest = 18 - parseInt(hole); 
//    lead;
    console.log("holes restant à jouer = " + rest);
// vérifier si l'avance d'une équipe est supérieure au nombre de trous restant à jouer !!!
     if(totA > totB)
     {   console.log("totA > totB");
         lead = totA - totB;
            console.info("lead = " + lead);
         if(lead > rest)
         {
             if (rest > 0)
             {
               //  return "Team A Wins " + lead + " & " + rest;
                 return nameA + " Wins " + lead + " & " + rest;
             }
             if (rest === 0)
             {  return nameA + " Wins " + lead + " UP";  }
          }
     }
     if(totB > totA)
     {   console.info("totB > totA");
         lead = totB - totA;
            console.info("lead = " + lead);
         if(lead > rest)
         {
             if (rest > 0)
             {
              //   return "Team B Wins " + lead + " & " + rest;
                 return nameB + " Wins " + lead + " & " + rest;
             }
             if (rest === 0)
             {  return nameB + " Wins " + lead + " UP" ; }
          }
     }
return "GO";
 }catch(error){
    console.error("LC : we found an error in mp_endCourse = " + error);}
}// end function mp_endCourse

function mp_initArray(rows, cols){
try{
     console.info(" function mp_init starting with rows = " + rows);
    var matrix = [];
    for (var i = 0; i < rows; i++)
    { 
        matrix[i] = []; 
        for (var j = 0; j < cols; j++)
        { 
            matrix[i][j] = 0;  // initialisé à 0
        } 
    }
//    console.info(" function mp_init ending matrix = " + matrix);
return matrix;
    }catch(error){
    console.error("LC : we found an error in mp_initArray = " + error);}
} // end function mp_init

function mp_colorScore(par,el){
  try{
        console.info(" function mp_colorScore starting with par = " + par + " /el = " + el.value);
    if(parseInt(el.value) === parseInt(par))
          {console.info(" color value = par");
           el.style.backgroundColor="lightblue";
           el.style.borderColor="red";
           el.style.borderWidth="2px";
          }
    if(parseInt(el.value) < parseInt(par))
          {console.info(" color value < par");
           el.style.backgroundColor="red";
           el.style.borderStyle="double";
           el.style.borderWidth="3px";
          }
    if(parseInt(el.value) > parseInt(par))
          {console.info(" color value > par");
           el.style.backgroundColor="green";
           el.style.borderStyle="solid";
           el.style.borderWidth="4px";
          }
 }catch(error){
    console.error("LC : we found an error in mp_colorScore = " + error);}
}// end function mp_colorScore

function mp_start(){ // variable globale (déclarées dans une fonction, sans 'var'
 try{   
    console.info("function mp_start, variable 'init' = " + init);
    document.getElementById("score_matchplay:buttonValidate").style.visibility = "hidden"; //"visible"
    document.getElementById("score_matchplay:buttonRegister").style.disabled = true; //boolean
    console.info("from mp_start, var 'init' =" + init);
   }catch(error){
    console.error("LC : we found an error in mp_start = " + error);}
}// end function mp_start

function mp_loadResult(){
  try{
    alert("starting function loadResult");
      // new 10/01/2015   pour être lu par JSF !       
 //   var b = MP4.join(';');
 //   console.error("LC : var b in mp_loadResult = " + b);
    
////    document.getElementById("score_matchplay:hiddenString").value = MP4.join(';'); //MP4.join(';'); // load hidden field - was innerHTML
////    document.getElementById("score_matchplay:roundResult").innerHTML = endC; 
////    document.getElementById("score_matchplay:hiddenResult").value = endC; // load hidden field
  }catch(error){
    console.error("LC : we found an error in mp_loadResult = " + error);  }
} // end function mp_loadResult

function mp_initialize(){
try{
    alert("starting mp_initialize " );
    init = 0;
    MP4 = mp_initArray(6,18);
    totA = 0, totB = 0;
//    hole, n, str;
    stop = "go on";
 //   p1,p2,p3,p4, player,score, par, valid,rest,lead;
    teamA = 0, teamB = 0; //, endC, R;
 //   game;
 }catch(error){
    console.error("LC : we found an error in mp_initialize = " + error);}
}// end function mp_initialize
//
//La fonction cherche tous éléments ayant comme sélecteur :input.
// Puisque nous lui passons l'argument #id-formulaire, la fonction ne cherchera que dans ce formulaire.
//Ensuite, la fonction filtre les boutons, les submits, les resets et les hidden inputs en utilisant not().
// Finalement, la fonction utilise val() pour donner une valeur nulle à tous les champs qui n'ont pas été écartés,
//  et utilise removeAttr pour enlever l'attribut checked et selected
//  au cas où le formulaire contient des champs radio/checkbox/select.
//http://itx-technologies.com/blog/2590-effacer-un-formulaire-avec-jquery

function clear_form(formulaire) //
{
    alert("starting clear_form with jquery = " + $.fn.jquery);
    if(window.mp_initialize) {  // si la fonction existe !!
        mp_initialize();
    }
 
//  $('INPUT:text, INPUT:password, INPUT:file, SELECT, TEXTAREA', '#myFormId').val('');
$(formulaire + ' :INPUT')
//$(':INPUT','#score_matchplay')
   .not(':button, :submit, :reset, :hidden, :radio, :checkbox select')
   .removeAttr('checked')
   .removeAttr('selected')
   .val('');
}
function ClearContact() {
           ("form :text").val("");
       }
//If i want to clear all the fields except accountType..Use the following
//$q(':input','#myform').not('#accountType').val('').removeAttr('checked').removeAttr('selected'

function clearForm(myFormElement) //not used !!
{
    alert("entering clearForm");
  var elements = myFormElement.elements;
  myFormElement.reset();
  for(i=0; i<elements.length; i++)
  {
      field_type = elements[i].type.toLowerCase();
      switch(field_type)
      {
        case "text":
        case "password":
        case "textarea":
        case "hidden":
          console.info("initialized = " + elements[i]);
          elements[i].value = "";
        break;

        case "radio":
        case "checkbox":
            if (elements[i].checked) {
                elements[i].checked = false;
            }
        break;

        case "select-one":
        case "select-multi":
           elements[i].selectedIndex = -1;
        break;

        default:
        break;
    } // end switch
  } // end for
}
// Focus = Changes the background color of input to yellow
function focusFunction(focus) {
    console.info("fonction focus : id = " + focus.id + " value = " + focus.value);
 //   document.getElementById("myInput").style.background = "yellow";
}

// No focus = Changes the background color of input to red
function blurFunction(blur) {
    console.info("fonction blur : id = " + blur.id + " value = " + blur.value);
 //   document.getElementById("myInput").style.background = "red";
}
function keydownFunction(keydown) {
     console.info("fonction keydown : id = " + keydown.id + " value = " + keydown.value);
   // document.getElementById("demo").style.backgroundColor = "red";
}

function keyupFunction(keyup) {
     console.info("fonction keyup : id = " + keyup.id + " value = " + keyup.value);
  //  document.getElementById("demo").style.backgroundColor = "green";
}
function referrer() {
    var x = document.referrer;
    document.getElementById("demo").innerHTML = x;
}
function lastmodified() {
    var x = document.lastModified;
    document.getElementById("demo").innerHTML = x;
}
function myFunction(el) {
    var x = document.documentURI;
    document.getElementById("demo").innerHTML = x;
}

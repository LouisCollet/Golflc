
// "use strict"; you should never use it globally !!
console.info("starting maps_travel_modes.js");
window.onerror = function (errorMsg, url, lineNumber, column, errorObj) {
    console.info('Error: ' + errorMsg + ' Script: ' + url + ' Line: ' + lineNumber
        + ' Column: ' + column + ' StackTrace: ' +  errorObj);
    alert('Error: ' + errorMsg + ' Script: ' + url + ' Line: ' + lineNumber
        + ' Column: ' + column + ' StackTrace: ' +  errorObj);
};
var google; // , eviter erreurs validateur
 //            console.info("js file : starting googleapis ");
 //       src="https://maps.googleapis.com/maps/api/js?v=3.exp&signed_in=true";       
// attendre le chargement ?
var directionsDisplay; // = new google.maps.DirectionsRenderer();
var directionsService; // = new google.maps.DirectionsService();
var map;
var origin; // = new google.maps.LatLng(50.826234,4.35712); // rue de l'amazone 55
//var origin = new google.maps.LatLng(37.7699298, -122.4469157);
//var oceanBeach = new google.maps.LatLng(37.7683909618184, -122.51089453697205);
var destination ;// = new google.maps.LatLng(50.729923, 4.599684); // golf la bawette
var travelMode;
var unitSystem;
var timeout = 600;

//function loadScript()
//
   "use strict"; 
     console.info("starting loadScript");
  var script = document.createElement('script');
  
  document.body.appendChild(script);
    console.info("ending loadScript");
//} //end loadScript

function initialize()
{
        "use strict";
        var google;
script.type = 'text/javascript';
  script.async="async";
  script.defer="defer";
  script.src = 'https://maps.googleapis.com/maps/api/js?key=AIzaSyACXDPdyVSXu-qCcvegAyoL2ykdbahQ3Lc&callback=initialize';
  

    console.info("starting initialize of maps_travel_modes.js");
     var outputDiv = document.getElementById("travel_modes:longitude");
     console.info(" var outputDiv " + outputDiv);
// var google is now defined
  directionsService = new google.maps.DirectionsService();
  directionsDisplay = new google.maps.DirectionsRenderer();
  origin = new google.maps.LatLng(50.826234,4.35712); // rue de l'amazone 55
  destination = new google.maps.LatLng(50.729923, 4.599684); // golf la bawette
//  travelMode = new google.maps.TravelMode.DRIVING;
//  unitSystem = new google.maps.UnitSystem.METRIC;
  var mapOptions = {
    zoom: 14,
    center: origin
  };
  map = new google.maps.Map(document.getElementById('map-canvas'), mapOptions);
  directionsDisplay.setMap(map);
  console.info("ending initialize of maps_travel_modes.js");
} //end initialize

function calcRoute() {
  //  "use strict";
    console.info("starting calcRoute of maps_travel_modes.js");
  //  console.info("origin = " + origin.toString());
  var selectedMode = document.getElementById('mode').value;
    console.info("selectedMode = " + selectedMode);
  var request = {
      origin: origin,
      destination: destination,
 //     travelMode: google.maps.TravelMode.DRIVING,
      unitSystem: unitSystem,
      avoidHighways: false,
      avoidTolls: false,
      // Note that Javascript allows us to access the constant
      // using square brackets and a string value as its "property."
      travelMode: google.maps.TravelMode[selectedMode]
     //  travelMode: travelMode
  };
  directionsService = new google.maps.DirectionsService();
  directionsService.route(request, function(response, status) // undefined !!!
  {
    if (status === google.maps.DirectionsStatus.OK)
    {
        console.info("calcRoute - Status Ok");
      directionsDisplay.setDirections(response);
    }else{
        console.info("calcRoute - directionsServices not OK = " + status);
        if (status === "OVER_QUERY_LIMIT")
            {
                setTimeout(function() { calcRoute(); }, (timeout));
                console.info("after setTimeout");
            }
    } //end else
  });
  console.info("ending calcRoute of maps_travel_modes.js");
} //end calcRoute
 console.info("end .js file ?? ");
///  not used if asynchronous google.maps.event.addDomListener(window, 'load', initialize);

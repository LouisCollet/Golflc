
// "use strict"; you should never use it globally !!
console.info("starting maps_home_club.js");
window.onerror = function (errorMsg, url, lineNumber, column, errorObj) {
    console.info('Error: ' + errorMsg + ' Script: ' + url + ' Line: ' + lineNumber
        + ' Column: ' + column + ' StackTrace: ' +  errorObj);
    alert('Error: ' + errorMsg + ' Script: ' + url + ' Line: ' + lineNumber
        + ' Column: ' + column + ' StackTrace: ' +  errorObj);
};
var google, source, destination, directionsService, directionsDisplay, amazone, mapOptions, map, request;
var timeout = 600;

function GetRoute() {
try{
   "use strict"; // new 30/07/2017
  //          alert("entering GetRoute");
            console.log("starting GetRoute of maps_home_club.js");
         directionsService = new google.maps.DirectionsService();
         directionsDisplay = new google.maps.DirectionsRenderer();
         amazone = new google.maps.LatLng(50.826258, 4.357085); // faudrait avoir une conversion adresse ++> latlng
         // faudrait avoir le domicile du joueur et pas toujours LC !!
   //       google.maps.event.addDomListener(window, 'load', function () {
 //           new google.maps.places.SearchBox(document.getElementById('calculate_distance:txtSource'));
 //           new google.maps.places.SearchBox(document.getElementById('calculate_distance:txtDestination'));
 //           directionsDisplay = new google.maps.DirectionsRenderer({ 'draggable': true });
 //         });
 //https://www.google.be/search?newwindow=1&site=&source=hp&q=contil+poisitionning+google+maps+javascript+api&oq=contil+poisitionning+google+maps+javascript+api&gs_l=psy-ab.3...4778.19395.0.21608.48.47.0.0.0.0.130.4359.30j16.46.0....0...1.1.64.psy-ab..2.42.3973.0..0j46j35i39k1j0i131k1j0i46k1j0i13k1j33i160k1j0i13i30k1j0i13i5i30k1j0i8i13i30k1j33i21k1.jUikxgxUGD8
            mapOptions = {
                zoom: 7,
                mapTypeId: google.maps.MapTypeId.ROADMAP,
                center: amazone,  // à adapter
                mapTypeControl: true,
                mapTypeControlOptions: {
                    style: google.maps.MapTypeControlStyle.HORIZONTAL_BAR,
                    position: google.maps.ControlPosition.TOP_CENTER
                },
                zoomControl: true,
                zoomControlOptions: {
                    position: google.maps.ControlPosition.LEFT_CENTER
                },
                scaleControl: true,
                streetViewControl: true,
                    streetViewControlOptions: { // https://developers.google.com/maps/documentation/javascript/streetview
              position: google.maps.ControlPosition.LEFT_TOP
          },
          fullscreenControl: true
            };
            map = new google.maps.Map(document.getElementById('dvMap'), mapOptions);
            directionsDisplay.setMap(map);
            directionsDisplay.setPanel(document.getElementById('dvPanel'));

            //*********DIRECTIONS AND ROUTE**********************//
            source = document.getElementById("calculate_distance:txtSource").value;
                console.log("source of maps_home_club.js = " + source);
            destination = document.getElementById("calculate_distance:txtDestination").value;
                console.log("destination of maps_home_club.js = " + destination);
            request = {
                origin: source,
                destination: destination,
                travelMode: google.maps.TravelMode.DRIVING
            };
     //       alert("request = " + request);
            directionsService.route(request, function (response, status) {
                if (status === google.maps.DirectionsStatus.OK)
                {
                    directionsDisplay.setDirections(response);
                }
            });

            //*********DISTANCE AND DURATION**********************//
            var service = new google.maps.DistanceMatrixService();
            service.getDistanceMatrix({
                origins: [source],
                destinations: [destination],
                travelMode: google.maps.TravelMode.DRIVING,
                unitSystem: google.maps.UnitSystem.METRIC,
                avoidHighways: false,
                avoidTolls: false
            }, function (response, status) {
                if (status === google.maps.DistanceMatrixStatus.OK) //&amp; (response.rows[0].elements[0].status !== "ZERO_RESULTS"))
                {
                    if(response.rows[0].elements[0].status !== "ZERO_RESULTS")
                    {   
                        console.info("RESULTS FOR maps_home_club.js");
                        var distance = response.rows[0].elements[0].distance.text;
                            console.log("distance of maps_home_club.js = " + distance);
                        var duration = response.rows[0].elements[0].duration.text;
                            console.log("duration of maps_home_club.js = " + duration);
                //        var dvDistance = document.getElementById("dvDistance");
                //        var dvDuration = document.getElementById("dvDuration");
                //        var distance02 = document.getElementById('calculate_distance:distance02');
                //        var duration02 = document.getElementById('calculate_distance:duration02');
                //        dvDistance.innerHTML = "";
                //        dvDistance.innerHTML += "Distance: " + distance + "<br />";
                //        dvDuration.innerHTML = "";
                //        dvDuration.innerHTML += "Duration: " + duration + "<br />";
                //        distance02.innerHTML = "";
                //        distance02.innerHTML += distance;
                //        duration02.innerHTML = "";
                //        duration02.innerHTML += duration;
                        document.getElementById('calculate_distance:distance02').value = distance;
                        document.getElementById('calculate_distance:duration02').value = duration;
                    }else{
                         alert("Sorry : Unable to find distance and duration - Please correct the Destination field");
                         console.error("Unable to find the distance via ZERO_RESULTS.");
                    }
                } else {
                    console.log("status DistanceMatrixStatus not OK");
                    if (status === "OVER_QUERY_LIMIT")
                        {
                        setTimeout(function() { calcRoute(); }, (timeout));
                        console.info("after setTimeout");
                        }
                }
            });
  }catch(exception){
    var message = exception.message;
        console.error("LC Exception initial variable definition = " + exception);
}           
 } //end function getRoute 
          //

 console.info("end .js file ?? ");
/* 
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
"use strict"; // new 21/07/2014 héhé
var map;
var google;
var lastLocation;
var mapOptions = "";
  $ = jQuery;
console.info("... entering timezone-api-sample"); 
function initialize()
{try{
    lastLocation = new google.maps.LatLng(35.68, 139.75);
    mapOptions = {
        zoom: 2,
        center: lastLocation,
        mapTypeId: google.maps.MapTypeId.ROADMAP
    };
    var obj = JSON.parse(mapOptions);
    console.info("var mapOptions = " + mapOptions);
    console.info("obj mapOptions = " + obj);
    //create map
    map = new google.maps.Map(document.getElementById('timezone-api-sample\\:map-canvas'), mapOptions);
    if (window.jQuery)  // jQuery is available.
    {
        console.info("Jquery version = " + jQuery.fn.jquery);
    }
    //click event
    setClickEvent();
}catch(exception){
    // http://www.sitepoint.com/exceptional-exception-handling-in-javascript/
    var message = "error in initialize = " + exception.message;
    console.info(message);
    alert(message);}    
} //end function initialize

function setClickEvent()
{try{
    google.maps.event.addListener(map, 'click', function(event) {
        var requestUrl =
            'https://maps.googleapis.com/maps/api/timezone/json' + //https://maps.googleapis.com/maps/api/timezone/json?location=39.6034810,-119.6822510&timestamp=1331161200&key=YOUR_API_KEY
            '?key=AIzaSyACXDPdyVSXu-qCcvegAyoL2ykdbahQ3Lc' +  // AIzaSyACXDPdyVSXu-qCcvegAyoL2ykdbahQ3Lc
            '&location=' + event.latLng.lat() + ',' + event.latLng.lng() +
            '&timestamp=' + getTimeStamp(new Date().getTime()) +
            '&language=' + 'en';
        console.info("requestURL = " + requestUrl); 
        //request timezone
        jQuery.ajax({
            url: requestUrl,
            type: 'GET',
            success: function(timeZone) {
                if (timeZone['status'] === 'OK') {
                    console.info("status =  OK"); 
                    addMarker(event.latLng, timeZone);
                } else {
                    //error
                    alert('status:' + timeZone['status']);
                }
            }
        });
    });
 }catch(exception){
    // http://www.sitepoint.com/exceptional-exception-handling-in-javascript/
    var message = "error in setClickEvent = " + exception.message;
    console.info(message);
    alert(message);}
} // end function

function getTimeStamp(time)
{
    return Math.round(time / 1000);
}

function addMarker(latLng, timeZone)
{ try{ 
        var contentString =
        '<div class="content">' +
        '<p>' + 'wat:　' + latLng + '</p>' +
        '<p>' + 'watID:　' + timeZone['timeZoneId'] + '</p>' +
        '<p>' + 'wat:　' + timeZone['timeZoneName'] + '</p>' +
        '<p>' + 'wat:　' + timeZone['rawOffset']/3600 + '??' + '</p>' +
        '<p>' + 'wat:　' + timeZone['dstOffset']/3600 + '??' + '</p>' +
        '</div>';
    console.info(contentString);
    //create infowindow
    var infowindow = new google.maps.InfoWindow({
        content: contentString
    });
    
    //create marker
    var marker = new google.maps.Marker({
        position:latLng
    });
    
    //set event
    google.maps.event.addListener(marker, 'click', function() {
        infowindow.open(map, marker);
    });
    
     marker.setMap(map);   //set map
     infowindow.open(map, marker);   //open infowindow
}catch(exception){
    var message = "error in addMarker = " + exception.message;
    console.info(message);
    alert(message);}
} // end function addMarker
google.maps.event.addDomListener(window, 'load', initialize);
//google.maps.event.addDomListener(window, 'load', initialize);
/* 
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
var map, google;

function initialize()
{
    lastLocation = new google.maps.LatLng(35.68, 139.75);
    var mapOptions = {
        zoom: 2,
        center: lastLocation,
        mapTypeId: google.maps.MapTypeId.ROADMAP
    };
    
    //create map
    map = new google.maps.Map(document.getElementById('map-canvas'), mapOptions);

    //click event
    setClickEvent();
}

function setClickEvent()
{
    google.maps.event.addListener(map, 'click', function(event) {
        var requestUrl =
            'https://maps.googleapis.com/maps/api/timezone/' +
            'json' +
            '?location=' + event.latLng.lat() + ',' + event.latLng.lng() +
            '&timestamp=' + getTimeStamp(new Date().getTime()) +
            '&sensor=' + 'false' +
            '&language=' + 'ja';

        //request timezone
        $.ajax({
            url: requestUrl,
            type: 'GET',
            success: function(timeZone) {
                if (timeZone['status'] === 'OK') {
                    //add marker
                    addMarker(event.latLng, timeZone);
                } else {
                    //error
                    alert('status:' + timeZone['status']);
                }
            }
        });
    });
}

function getTimeStamp(time)
{
    return Math.round(time / 1000);
}

function addMarker(latLng, timeZone)
{
    try{    var contentString =
        '<div class="content">' +
        '<p>' + 'wat:　' + latLng + '</p>' +
        '<p>' + 'watID:　' + timeZone['timeZoneId'] + '</p>' +
        '<p>' + 'wat:　' + timeZone['timeZoneName'] + '</p>' +
        '<p>' + 'wat:　' + timeZone['rawOffset']/3600 + '??' + '</p>' +
        '<p>' + 'wat:　' + timeZone['dstOffset']/3600 + '??' + '</p>' +
        '</div>';
alert(contentString);
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
    
    //set map
    marker.setMap(map);
    
    //open infowindow
    infowindow.open(map, marker);
// end function addmarker
}catch(exception){
    // http://www.sitepoint.com/exceptional-exception-handling-in-javascript/
    var message = exception.message;
    alert(message);
}
} // end function addMarker
google.maps.event.addDomListener(window, 'load', initialize);
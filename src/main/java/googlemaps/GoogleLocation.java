package googlemaps;

import com.google.maps.model.LatLng;

public class GoogleLocation 
{

    public GoogleLocation() // constructor
{
    
}
    
    
    
 private String lat;
 private String lng;
 private LatLng latLng;
 double latitude;
 double longitude;

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }
 
 
    public LatLng getLatlng() {
        latitude = Double.parseDouble(lat);
        longitude = Double.parseDouble(lng);
        LatLng latlng = new LatLng(latitude, longitude);
        
        return latlng;
    }

    public void setLatlng(LatLng latlng) {
        this.latLng = latlng;
    }

 public String getLat() {
  return lat;
 }

 public void setLat(String lat) {
  this.lat = lat;
 }

 public String getLng() {
  return lng;
 }

 public void setLng(String lng) {
  this.lng = lng;
 }
 
} // end class

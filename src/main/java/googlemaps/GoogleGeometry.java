package googlemaps;

//Geometry class will have location and other details .


public class GoogleGeometry {

private GoogleLocation location ;
private String location_type;
private Object bounds;
private GoogleTimeZone timezone ;
 
 private Object viewport;

    public GoogleTimeZone getTimezone() {
        return timezone;
    }

    public void setTimezone(GoogleTimeZone timezone) {
        this.timezone = timezone;
    }

 
 public GoogleLocation getLocation() {
  return location;
 }

 public void setLocation(GoogleLocation location) {
  this.location = location;
 }

 public String getLocation_type() {
  return location_type;
 }

 public void setLocation_type(String location_type) {
  this.location_type = location_type;
 }

 public Object getBounds() {
  return bounds;
 }

 public void setBounds(Object bounds) {
  this.bounds = bounds;
 }

 public Object getViewport() {
  return viewport;
 }

 public void setViewport(Object viewport) {
  this.viewport = viewport;
 }
} //end class
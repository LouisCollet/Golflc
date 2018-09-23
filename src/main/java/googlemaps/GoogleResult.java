package googlemaps;

public class GoogleResult {

private String formatted_address;
private boolean partial_match;
private GoogleGeometry geometry;
private Object address_components;
private Object types;
//private GoogleTimeZone timezone;

 public String getFormatted_address() {
  return formatted_address;
 }

 public void setFormatted_address(String formatted_address) {
  this.formatted_address = formatted_address;
 }

 public boolean isPartial_match() {
  return partial_match;
 }

 public void setPartial_match(boolean partial_match) {
  this.partial_match = partial_match;
 }

 public GoogleGeometry getGeometry() {
  return geometry;
 }

 public void setGeometry(GoogleGeometry geometry) {
  this.geometry = geometry;
 }
 
 public Object getAddress_components() {
  return address_components;
 }
 
 public void setAddress_components(Object address_components) {
  this.address_components = address_components;
 }

 public Object getTypes() {
  return types;
 }

 public void setTypes(Object types) {
  this.types = types;
 }
  @Override
public String toString()
{ return 
        ("from " + getClass().getSimpleName() + " : "
           //    + " ,status : "   + this.getFormatted_address()
               + " ,formatted adress : "   + this.getFormatted_address()
           //    + " ,geometry : " + this.getGeometry()
               + " ,geometry location Lat : " + this.getGeometry().getLocation().getLat()
               + " ,geometry location Longt : " + this.getGeometry().getLocation().getLng()
               + " ,geometry location type : " + this.getGeometry().getLocation_type()
               + " ,geometry location viewport : " + this.getGeometry().getViewport()
        + " ,types : " + this.getTypes()
        );
}   
 
 } //end class
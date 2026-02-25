
package entite;

import static interfaces.Log.LOG;
import java.sql.ResultSet;
import java.sql.SQLException;
import utils.LCUtil;

public class LatLng implements  interfaces.Log{
    private static final long serialVersionUID = 1L;
    private final static String CLASSNAME = utils.LCUtil.getCurrentClassName();

    private double lat;
    private double lng;
    public LatLng(double la, double ln) {// constructor
      this.lat = la;
      this.lng = ln;
    }

    public LatLng() {// default constructor
    }
    
    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLng() {
        return lng;
    }

    public void setLng(double lng) {
        this.lng = lng;
    }

 @Override
public String toString(){
 try{   
//    LOG.debug("starting toString Hole!");
    if(this.getClass() == null){
         return ("LatLng is null, no print !");
    }
    return 
        (NEW_LINE + TAB
      //      + "\033[33mYellow Submarine"
            //https://www.geeksforgeeks.org/how-to-print-colored-text-in-java-console/
         //   + RED + 
             + TAB + "FROM ENTITE : "+ this.getClass().getSimpleName().toUpperCase()
        //    + RESET 
            + TAB
               + " lat : " + this.lat
               + " ,lng : " + this.lng
         );
    }catch(Exception e){
        String msg = "£££ Exception in LatLng.toString = " + e.getMessage();
        LOG.error(msg);
        LCUtil.showMessageFatal(msg);
        return msg;
  }
}

  public static LatLng mapPlayer(ResultSet rs) throws SQLException{ // coming from Player.map
      final String methodName = utils.LCUtil.getCurrentMethodName(CLASSNAME); 
  try{
    //       LOG.debug("entering LatLng mapPlayer");
       String[] laln = null;
       if(rs.getString("PlayerLatLng") == null){ 
           laln = "50.8262271,4.3571382".split(",");
    //       LOG.debug("laln[] forced to default"); // le même pour tous ! par defaut
       }else{
           laln = rs.getString("PlayerLatLng").split(",");
       }
      LatLng latlng = new LatLng();
      latlng.setLat(Double.parseDouble(laln[0]));
      latlng.setLng(Double.parseDouble(laln[1]));
        return latlng;
  }catch(Exception e){
      String msg = "£££ Exception in rs = " + methodName + " /" + e.getMessage();
      LOG.error(msg);
      LCUtil.showMessageFatal(msg);
      return null;
  }
} //end method map
  
  public static LatLng mapClub(ResultSet rs) throws SQLException{
      final String methodName = utils.LCUtil.getCurrentMethodName(CLASSNAME); 
  try{
        LatLng latlng = new LatLng();
        latlng.setLat(rs.getDouble("ClubLatitude") );
        latlng.setLng(rs.getDouble("ClubLongitude") );
        return latlng;
  }catch(Exception e){
   String msg = "£££ Exception in rs = " + methodName + " /" + e.getMessage();
   LOG.error(msg);
    LCUtil.showMessageFatal(msg);
    return null;
  }
} //end method map

} // end class
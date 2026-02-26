
package rowmappers;

import entite.LatLng;
import static exceptions.LCException.handleGenericException;
import java.sql.ResultSet;
import java.sql.SQLException;

public class LatlngPlayerRowMapper extends AbstractRowMapper<LatLng> {

    @Override
    public LatLng map(ResultSet rs) throws SQLException {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        try {
            String latLngStr = getString(rs, "PlayerLatLng");
            if (latLngStr == null || latLngStr.isBlank()) {
                latLngStr = "50.8262271,4.3571382"; // default
            }

            String[] parts = latLngStr.split(",");
            LatLng latLng = new LatLng();
            latLng.setLat(Double.parseDouble(parts[0]));
            latLng.setLng(Double.parseDouble(parts[1]));

            return latLng;

        } catch (Exception e) {
            handleGenericException(e, methodName);
            return null;
        }
    }
}

/*
import entite.LatLng;
import static exceptions.LCException.handleGenericException;
import static interfaces.Log.LOG;
import java.sql.ResultSet;
import java.sql.SQLException;

public class LatlngPlayerRowMapper implements RowMapper<LatLng> {
 
    @Override
   public LatLng map(ResultSet rs) throws SQLException {
       final String methodName = utils.LCUtil.getCurrentMethodName();
 try{  
      String[] laln = null;
       if(rs.getString("PlayerLatLng") == null){ 
           laln = "50.8262271,4.3571382".split(",");
    //       LOG.debug("laln[] forced to default"); // le même pour tous ! par defaut
       }else{
           laln = rs.getString("PlayerLatLng").split(",");
       }
      LatLng latLng = new LatLng();
      latLng.setLat(Double.parseDouble(laln[0]));
      latLng.setLng(Double.parseDouble(laln[1]));
   return latLng;
 }catch(Exception e){
    handleGenericException(e, methodName);
    return null;
 }
} //end method
} // end class */
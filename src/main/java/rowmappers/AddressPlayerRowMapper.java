
package rowmappers;

import entite.Address;
import entite.LatLng;
import static exceptions.LCException.handleGenericException;
import java.sql.ResultSet;
import java.sql.SQLException;

public class AddressPlayerRowMapper extends AbstractRowMapper<Address> {

    @Override
    public Address map(ResultSet rs) throws SQLException {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        try {
            Address address = new Address();
            address.setStreet(getString(rs, "PlayerStreet"));
            address.setCity(getString(rs, "PlayerCity"));
            address.getCountry().setCode(getString(rs, "PlayerCountry"));

            RowMapper<LatLng> mapper = new LatlngPlayerRowMapper();
            address.setLatLng(mapper.map(rs));

            address.setZoneId(getString(rs, "PlayerZoneId"));
            if (address.getZoneId() == null) {
                address.setZoneId("Europe/Brussels");
            }

            return address;

        } catch (Exception e) {
            handleGenericException(e, methodName);
            return null;
        }
    }
}

/*
import entite.Address;
import entite.LatLng;
import static exceptions.LCException.handleGenericException;
import static interfaces.Log.LOG;
import java.sql.ResultSet;
import java.sql.SQLException;

public class AddressPlayerRowMapper implements RowMapper<Address> {
 
    @Override
   public Address map(ResultSet rs) throws SQLException {
       final String methodName = utils.LCUtil.getCurrentMethodName();
   try{  
           //LOG.debug("entering map for method = " + methodName);
        Address address = new Address();
        address.setStreet(rs.getString("PlayerStreet"));
        address.setCity(rs.getString("PlayerCity"));
        address.getCountry().setCode(rs.getString("PlayerCountry"));
      //  LatLng latLng = LatLng.mapPlayer(rs); // ajout 06-12-2023
        RowMapper<LatLng> mapper = new LatlngPlayerRowMapper();
        address.setLatLng(mapper.map(rs)); // mod 28-12-2025 non testé
//        LatLng latLng = LatLng.mapPlayer(rs); // mod 28-12-2025
//        address.setLatLng(latLng);
        address.setZoneId(rs.getString("PlayerZoneId"));
        if(address.getZoneId() == null){
           address.setZoneId("Europe/Brussels");
        }
   return address;
 }catch(Exception e){
        handleGenericException(e, methodName);
    return null;
 }
} //end method
} // end class
*/
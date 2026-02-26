
package rowmappers;

import entite.Address;
import entite.LatLng;
import static exceptions.LCException.handleGenericException;
import static interfaces.Log.LOG;
import java.sql.ResultSet;
import java.sql.SQLException;

public class AddressClubRowMapper extends AbstractRowMapper<Address> {

    private static final String CLASSNAME = utils.LCUtil.getCurrentClassName();

    @Override
    public Address map(ResultSet rs) throws SQLException {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        try {
            var address = new Address();

            address.setStreet(getString(rs, "clubAddress"));
            address.setCity(getString(rs, "clubCity"));

            // ⚠️ Sécurisation : country peut être null selon le constructeur
            if (address.getCountry() != null) {
                address.getCountry().setCode(getString(rs, "clubCountry"));
            }

            // Mapping LatLng via RowMapper dédié
            RowMapper<LatLng> latLngMapper = new LatlngClubRowMapper();
            address.setLatLng(latLngMapper.map(rs));

            address.setZoneId(getString(rs, "ClubZoneId"));

            return address;

        } catch (Exception e) {
            handleGenericException(e, methodName);
            return null;
        }
    }
}

/*
public class AddressClubRowMapper implements RowMapper<Address> {
 
    @Override
   public Address map(ResultSet rs) throws SQLException {
       final String methodName = utils.LCUtil.getCurrentMethodName();
   try{  
           //LOG.debug("entering map for method = " + methodName);
        Address address = new Address();
         address.setStreet(rs.getString("clubAddress") );
         address.setCity(rs.getString("clubCity"));
         address.getCountry().setCode(rs.getString("clubCountry") );
      //  LatLng latLng = LatLng.mapClub(rs);  // ajout 06-12-2023
      //   address.setLatLng(latLng); // old
        RowMapper<LatLng> mapper = new LatlngClubRowMapper();
         address.setLatLng(mapper.map(rs)); // mod 28-12-2025 non testé
         address.setZoneId(rs.getString("ClubZoneId"));
   return address;
 }catch(Exception e){
    handleGenericException(e, methodName);
    return null;
 }
} //end method
} // end class
*/
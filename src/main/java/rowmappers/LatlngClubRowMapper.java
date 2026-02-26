package rowmappers;

import entite.LatLng;
import java.sql.ResultSet;
import java.sql.SQLException;
import static exceptions.LCException.handleGenericException;

public class LatlngClubRowMapper extends AbstractRowMapper<LatLng> {

    @Override
    public LatLng map(ResultSet rs) throws SQLException {

        final String methodName = utils.LCUtil.getCurrentMethodName();

        try {
            Double lat = getDouble(rs, "ClubLatitude");
            Double lng = getDouble(rs, "ClubLongitude");

            // Cas métier : coordonnées absentes
            if (lat == null || lng == null) {
                return null; // ou new LatLng(null, null) selon besoin
            }

            return new LatLng(lat, lng);

        } catch (SQLException e) {
            throw e; // JDBC : on remonte
        } catch (Exception e) {
            handleGenericException(e, methodName);
            return null;
        }
    }
}


/*
public class LatlngClubRowMapper extends AbstractRowMapper<LatLng> {

    @Override
    public LatLng map(ResultSet rs) throws SQLException {
        final String methodName = utils.LCUtil.getCurrentMethodName();

        try {
            var latLng = new LatLng();
            latLng.setLat(getDouble(rs, "ClubLatitude"));
            latLng.setLng(getDouble(rs, "ClubLongitude"));
            return latLng;

        } catch (Exception e) {
            handleGenericException(e, methodName);
            return null;
        }
    }
}
*/

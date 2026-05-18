
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
} // end class

package rowmappers;

import entite.Handicap;
import static exceptions.LCException.handleGenericException;
import java.sql.ResultSet;
import java.sql.SQLException;
import static utils.LCUtil.getCurrentMethodName;

public class HandicapRowMapper extends AbstractRowMapper<Handicap> {

    @Override
    public Handicap map(ResultSet rs) throws SQLException {
        try {
            Handicap h = new Handicap();
            h.setHandicapStart(rs.getDate("idhandicap"));
            h.setHandicapEnd(rs.getDate("HandicapEnd"));
            h.setHandicapPlayerEGA(getBigDecimal(rs, "HandicapPlayerEGA"));
            return h;
        } catch (Exception e) {
            handleGenericException(e, getCurrentMethodName());
            return null;
        }
    } // end map

} // end class

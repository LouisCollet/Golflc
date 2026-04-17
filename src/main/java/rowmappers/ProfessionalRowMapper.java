package rowmappers;

import entite.Professional;
import static exceptions.LCException.handleGenericException;
import java.sql.ResultSet;
import java.sql.SQLException;
import static utils.LCUtil.getCurrentMethodName;

public class ProfessionalRowMapper extends AbstractRowMapper<Professional> {

    @Override
    public Professional map(ResultSet rs) throws SQLException {
 //       final String methodName = utils.LCUtil.getCurrentMethodName();
      try {
            Professional pro = new Professional();
            pro.setProId(getInteger(rs,"ProId"));
            pro.setProClubId(getInteger(rs,"ProClubId"));
            pro.setProStartDate(getTimestamp(rs,"ProClubStartDate").toLocalDateTime());
            pro.setProEndDate(getTimestamp(rs,"ProClubEndDate").toLocalDateTime());
            pro.setProPlayerId(getInteger(rs,"ProPlayerId"));
            pro.setProAmount(getDouble(rs,"ProAmount"));
            pro.setProWorkDays(rs.getInt("ProWorkDays"));
    //           LOG.debug("Professional event returned from map = " + pro);
            return pro;

        } catch (Exception e) {
            handleGenericException(e, getCurrentMethodName());
            return null;
        }
    } // end map
} // end class

package rowmappers;
import com.fasterxml.jackson.databind.ObjectMapper;
import entite.UnavailablePeriod;
import static exceptions.LCException.handleGenericException;
import static interfaces.Log.LOG;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UnavailablePeriodRowMapper extends AbstractRowMapper<UnavailablePeriod> {

    @Override
    public UnavailablePeriod map(ResultSet rs) throws SQLException {
        final String methodName = utils.LCUtil.getCurrentMethodName();
     try {
        ObjectMapper om = new ObjectMapper();
        UnavailablePeriod period = om.readValue(rs.getString("UnavailableItems"), UnavailablePeriod.class);
        period.setIdclub(getInteger(rs,"UnavailableIdClub"));
        period.setStartDate(getTimestamp(rs,"UnavailableStartDate").toLocalDateTime());
   //        LOG.debug("start date column = " + rs.getTimestamp("UnavailableStartDate").toLocalDateTime());
        period.setEndDate(getTimestamp(rs,"UnavailableEndDate").toLocalDateTime());
   //        LOG.debug("end date column = " + rs.getTimestamp("UnavailableEndDate").toLocalDateTime());
   return period;
    } catch (Exception e) {
        handleGenericException(e, methodName);
    return null;
        }
    }
}
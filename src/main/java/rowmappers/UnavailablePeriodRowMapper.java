
package rowmappers;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import entite.UnavailablePeriod;
import static exceptions.LCException.handleGenericException;
import static interfaces.Log.LOG;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UnavailablePeriodRowMapper extends AbstractRowMapper<UnavailablePeriod> {

    private static final ObjectMapper OBJECT_MAPPER;

    static {
        OBJECT_MAPPER = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
        OBJECT_MAPPER.registerModule(new JavaTimeModule());
        OBJECT_MAPPER.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
    }

    @Override
    public UnavailablePeriod map(ResultSet rs) throws SQLException {
        final String methodName = utils.LCUtil.getCurrentMethodName();
     try {
        UnavailablePeriod period = OBJECT_MAPPER.readValue(rs.getString("UnavailableItems"), UnavailablePeriod.class);
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
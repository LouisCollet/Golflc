package sql.preparedstatement;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import entite.UnavailablePeriod;
import static exceptions.LCException.handleGenericException;
import static interfaces.Log.LOG;
import java.io.Serializable;
import java.sql.JDBCType;
import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

public class psCreateUpdateUnavailablePeriod implements Serializable {

    private static final long serialVersionUID = 1L;

    private static final ObjectMapper OBJECT_MAPPER;
    static {
        OBJECT_MAPPER = new ObjectMapper();
        OBJECT_MAPPER.registerModule(new JavaTimeModule());
        OBJECT_MAPPER.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        OBJECT_MAPPER.configure(SerializationFeature.INDENT_OUTPUT, true);
        OBJECT_MAPPER.configure(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS, true);
    }

    public static PreparedStatement psMapCreate(
            PreparedStatement ps,
            final UnavailablePeriod unavailable) throws Exception {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        try {
            String json = OBJECT_MAPPER.writeValueAsString(unavailable);
            ps.setNull     (1, java.sql.Types.INTEGER);                // UnavailableId — auto-increment
            ps.setInt      (2, unavailable.getIdclub());
            ps.setObject   (3, unavailable.getStartDate(), JDBCType.TIMESTAMP);
            ps.setTimestamp(4, Timestamp.valueOf(unavailable.getEndDate()));
            ps.setString   (5, json);
            ps.setTimestamp(6, Timestamp.from(Instant.now()));         // ModificationDate
            sql.PrintWarnings.print(ps.getWarnings(), methodName);
            utils.LCUtil.logps(ps);
            return ps;
        } catch (Exception e) {
            handleGenericException(e, methodName);
            return null;
        }
    } // end method

    public static PreparedStatement psMapUpdate(
            PreparedStatement ps,
            final UnavailablePeriod period) throws Exception {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        try {
            String json = OBJECT_MAPPER.writeValueAsString(period);
            ps.setString   (1, json);
            ps.setTimestamp(2, toTimestamp(period.getStartDate()));
            ps.setTimestamp(3, toTimestamp(period.getEndDate()));
            ps.setTimestamp(4, Timestamp.from(Instant.now()));         // ModificationDate
            ps.setInt      (5, period.getIdclub());                    // WHERE
            sql.PrintWarnings.print(ps.getWarnings(), methodName);
            utils.LCUtil.logps(ps);
            return ps;
        } catch (Exception e) {
            handleGenericException(e, methodName);
            return null;
        }
    } // end method

    private static Timestamp toTimestamp(LocalDateTime ldt) {
        if (ldt == null) return null;
        return Timestamp.from(ldt.atZone(ZoneId.systemDefault()).toInstant());
    } // end method

    /*
    void main() {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
    } // end main
    */

} // end class

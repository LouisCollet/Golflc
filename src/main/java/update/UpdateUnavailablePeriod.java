package update;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import entite.UnavailablePeriod;
import static exceptions.LCException.handleGenericException;
import static exceptions.LCException.handleSQLException;
import static interfaces.Log.LOG;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import utils.LCUtil;

@ApplicationScoped
public class UpdateUnavailablePeriod implements Serializable {

    private static final long serialVersionUID = 1L;

    private static final ObjectMapper OBJECT_MAPPER;
    static {
        OBJECT_MAPPER = new ObjectMapper();
        OBJECT_MAPPER.registerModule(new JavaTimeModule());
        OBJECT_MAPPER.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        OBJECT_MAPPER.configure(SerializationFeature.INDENT_OUTPUT, true);
        OBJECT_MAPPER.configure(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS, true);
    }

    @Inject private dao.GenericDAO dao;

    public UpdateUnavailablePeriod() { }

    public boolean updateAvailability(final UnavailablePeriod period) throws SQLException {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);

        final String query = """
            UPDATE unavailable_periods
            SET UnavailableItems = ?,
                UnavailableStartDate = ?,
                UnavailableEndDate = ?,
                UnavailableModificationDate = ?
            WHERE UnavailableIdClub = ?
            ORDER BY UnavailableModificationDate DESC
            LIMIT 1
            """;

        String json;
        try {
            json = OBJECT_MAPPER.writeValueAsString(period);
        } catch (Exception ex) {
            handleGenericException(ex, methodName);
            return false;
        }
        LOG.debug("updating unavailableItems JSON = {}", json);

        try (Connection conn = dao.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setString(1, json);
            ps.setTimestamp(2, toTimestamp(period.getStartDate()));
            ps.setTimestamp(3, toTimestamp(period.getEndDate()));
            ps.setTimestamp(4, Timestamp.from(Instant.now()));
            ps.setInt(5, period.getIdclub());
            LCUtil.logps(ps);

            int row = ps.executeUpdate();
            LOG.debug("rows updated = {}", row);
            return row > 0;

        } catch (SQLException e) {
            handleSQLException(e, methodName);
            return false;
        } catch (Exception e) {
            handleGenericException(e, methodName);
            return false;
        }
    } // end method

    private Timestamp toTimestamp(LocalDateTime ldt) {
        if (ldt == null) return null;
        return Timestamp.from(ldt.atZone(ZoneId.systemDefault()).toInstant());
    } // end method

} // end class

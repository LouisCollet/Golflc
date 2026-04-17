package create;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import entite.UnavailablePeriod;
import entite.ValidationsLC;
import entite.ValidationsLC.ValidationStatus;
import static exceptions.LCException.handleGenericException;
import static exceptions.LCException.handleSQLException;
import static interfaces.Log.LOG;
import static interfaces.Log.NEW_LINE;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.JDBCType;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import utils.LCUtil;

@ApplicationScoped
public class CreateUnavailablePeriod implements Serializable {

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

    public CreateUnavailablePeriod() { }

    public boolean create(final UnavailablePeriod unavailable) throws SQLException {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        LOG.debug("with unavailable = {}", unavailable);

        ValidationsLC vlc = this.validate(unavailable);
        LOG.debug("validation result = {}", vlc);
        if (vlc == null || vlc.getStatus0().equals(ValidationStatus.REJECTED.toString())) {
            String err = (vlc != null) ? vlc.getStatus1() : "Validation returned null";
            LOG.error(err);
            LCUtil.showMessageFatal(err);
            return false;
        }

        try (Connection conn = dao.getConnection()) {
            unavailable.setItemPeriod(utils.LCUtil.removeNull1DBoolean(unavailable.getItemPeriod()));
            String json;
            try {
                json = OBJECT_MAPPER.writeValueAsString(unavailable);
            } catch (Exception ex) {
                handleGenericException(ex, methodName);
                return false;
            }
            LOG.debug("Unavailable Period converted in json format = {}", NEW_LINE + json);

            final String query = LCUtil.generateInsertQuery(conn, "unavailable_periods");
            try (PreparedStatement ps = conn.prepareStatement(query)) {
                ps.setNull(1, java.sql.Types.INTEGER);  // autoincrement
                ps.setInt(2, unavailable.getIdclub());
                ps.setObject(3, unavailable.getStartDate(), JDBCType.TIMESTAMP);
                ps.setTimestamp(4, Timestamp.valueOf(unavailable.getEndDate()));
                ps.setString(5, json);
                ps.setTimestamp(6, Timestamp.from(Instant.now()));
                utils.LCUtil.logps(ps);
                int row = ps.executeUpdate();
                LOG.debug("row created = {}", row);
                if (row != 0) {
                    String msg = "Unavailable Created for <br/>Course = " + unavailable.getIdclub();
                    LOG.debug(msg);
                    return true;
                } else {
                    String msg = "<br/><br/>NOT NOT Successful insert for unavailable = ";
                    LOG.debug(msg);
                    LCUtil.showMessageFatal(msg);
                    return false;
                }
            }
        } catch (SQLException e) {
            handleSQLException(e, methodName);
            return false;
        } catch (Exception e) {
            handleGenericException(e, methodName);
            return false;
        }
    } // end method

    public ValidationsLC validate(final UnavailablePeriod unavailable) throws SQLException {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        try {
            ValidationsLC vlc = new ValidationsLC();
            vlc.setStatus0(ValidationStatus.APPROVED.toString());
            if (unavailable.getEndDate().isBefore(unavailable.getStartDate())) {
                vlc.setStatus0(ValidationStatus.REJECTED.toString());
                String msgerr = LCUtil.prepareMessageBean("tarif.member.endbeforestart");
                vlc.setStatus1(msgerr);
            }
            return vlc;
        } catch (Exception e) {
            handleGenericException(e, methodName);
            return null;
        }
    } // end method

    /*
    void main() {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        UnavailablePeriod unavailable = new UnavailablePeriod();
        unavailable.setIdclub(1006);
        unavailable.setStartDate(LocalDateTime.parse("2020-11-03T12:45:30"));
        unavailable.setEndDate(LocalDateTime.parse("2020-10-04T12:45:30"));
        boolean lp = new CreateUnavailablePeriod().create(unavailable);
        LOG.debug("from main, after lp = {}", lp);
    } // end main
    */

} // end class

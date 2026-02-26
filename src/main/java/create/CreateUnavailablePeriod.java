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
import jakarta.annotation.Resource;
import jakarta.enterprise.context.ApplicationScoped;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.JDBCType;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import javax.sql.DataSource;
import utils.LCUtil;

@ApplicationScoped
public class CreateUnavailablePeriod implements Serializable {

    private static final long serialVersionUID = 1L;

    @Resource(lookup = "java:jboss/datasources/golflc")
    private DataSource dataSource;

    public CreateUnavailablePeriod() { }

    public boolean create(final UnavailablePeriod unavailable) throws SQLException {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName);
        LOG.debug("with unavailable = " + unavailable);

        ValidationsLC vlc = this.validate(unavailable);
        LOG.debug("validation result = " + vlc);
        if (vlc == null || vlc.getStatus0().equals(ValidationStatus.REJECTED.toString())) {
            String err = (vlc != null) ? vlc.getStatus1() : "Validation returned null";
            LOG.error(err);
            LCUtil.showMessageFatal(err);
            return false;
        }

        ObjectMapper om = new ObjectMapper();
        om.registerModule(new JavaTimeModule());
        om.configure(SerializationFeature.INDENT_OUTPUT, true);
        om.configure(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS, true);

        try (Connection conn = dataSource.getConnection()) {
            unavailable.setItemPeriod(utils.LCUtil.removeNull1DBoolean(unavailable.getItemPeriod()));
            String json;
            try {
                json = om.writeValueAsString(unavailable);
            } catch (Exception ex) {
                handleGenericException(ex, methodName);
                return false;
            }
            LOG.debug("Unavailable Period converted in json format = " + NEW_LINE + json);

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
                LOG.debug("row created = " + row);
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
        LOG.debug("entering " + methodName);
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
        LOG.debug("entering " + methodName);
        UnavailablePeriod unavailable = new UnavailablePeriod();
        unavailable.setIdclub(1006);
        unavailable.setStartDate(LocalDateTime.parse("2020-11-03T12:45:30"));
        unavailable.setEndDate(LocalDateTime.parse("2020-10-04T12:45:30"));
        boolean lp = new CreateUnavailablePeriod().create(unavailable);
        LOG.debug("from main, after lp = " + lp);
    } // end main
    */

} // end class

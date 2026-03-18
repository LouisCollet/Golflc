package create;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import entite.TarifMember;
import static exceptions.LCException.handleGenericException;
import static exceptions.LCException.handleSQLException;
import static interfaces.Log.LOG;
import static interfaces.Log.NEW_LINE;
import jakarta.annotation.Resource;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import javax.sql.DataSource;
import utils.LCUtil;

@ApplicationScoped
public class CreateTarifMember implements Serializable {

    private static final long serialVersionUID = 1L;

    @Resource(lookup = "java:jboss/datasources/golflc")
    private DataSource dataSource;

    @Inject
    private find.FindTarifMembersOverlapping findTarifMembersOverlapping;

    public CreateTarifMember() { }

    public boolean create(final TarifMember tarif) throws SQLException {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName);
        LOG.debug("with tarif = " + tarif);

        if (findTarifMembersOverlapping.find(tarif)) {
            return false; // rejected for dates overlapping
        }
        if (tarif.getBasicList().get(0).getEndDate().isBefore(tarif.getBasicList().get(0).getStartDate())) {
            String msgerr = LCUtil.prepareMessageBean("tarif.member.endbeforestart");
            LOG.error(msgerr);
            LCUtil.showMessageFatal(msgerr);
            return false;
        }

        ObjectMapper om = new ObjectMapper();
        om.registerModule(new JavaTimeModule());
        om.configure(SerializationFeature.INDENT_OUTPUT, true);
        om.configure(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS, true);
        String tarifJson;
        try {
            tarifJson = om.writeValueAsString(tarif);
        } catch (Exception ex) {
            handleGenericException(ex, methodName);
            return false;
        }
        LOG.debug("Tarif Member converted in json format = " + NEW_LINE + tarifJson);

        try (Connection conn = dataSource.getConnection()) {
        //    final String query = LCUtil.generateInsertQuery(conn, "tarif_members");
            try (PreparedStatement ps = conn.prepareStatement(sql.SqlFactory.generateInsertQuery(conn, "tarif_members"))) {
                ps.setNull(1, java.sql.Types.INTEGER);  // autoincrement
                ps.setTimestamp(2, Timestamp.valueOf(tarif.getStartDate()));
                ps.setTimestamp(3, Timestamp.valueOf(tarif.getEndDate()));
                ps.setInt(4, tarif.getTarifMemberIdClub());
                ps.setString(5, tarifJson);
                ps.setTimestamp(6, Timestamp.from(Instant.now()));
                utils.LCUtil.logps(ps);
                int row = ps.executeUpdate();
                if (row != 0) {
                    String msg = "Tarif Member Created  = <br/>" + tarif;
                    LOG.info(msg);
                    LCUtil.showMessageInfo(msg);
                    return true;
                } else {
                    String msg = "<br/><br/>ERROR insert for tarif : " + tarif;
                    LOG.error(msg);
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

    /*
    void main() {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName);
    } // end main
    */

} // end class

package find;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import entite.Round;
import entite.TarifGreenfee;
import static interfaces.Log.LOG;
import static exceptions.LCException.handleGenericException;
import static exceptions.LCException.handleSQLException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import utils.LCUtil;

@ApplicationScoped
public class FindTarifGreenfeeData implements Serializable, interfaces.GolfInterface {

    private static final long serialVersionUID = 1L;

    @Inject private dao.GenericDAO dao;

    public FindTarifGreenfeeData() { }

    public TarifGreenfee find(final Round round) throws SQLException {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName);
        LOG.debug(methodName + " - for round = " + round);

        if (round == null || round.getCourseIdcourse() == null) {
            String msg = LCUtil.prepareMessageBean("tarif.greenfee.notfound") + " (no course selected)";
            LOG.warn(methodName + " - " + msg);
            LCUtil.showMessageFatal(msg);
            return null;
        }

        final String query = """
            SELECT TarifJson
            FROM tarif_greenfee
            WHERE tarif_greenfee.TarifCourseId = ?
            AND ? BETWEEN tarif_greenfee.TarifStartDate AND tarif_greenfee.TarifEndDate
            """;

        try (Connection conn = dao.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setInt(1, round.getCourseIdcourse());
            ps.setTimestamp(2, Timestamp.valueOf(round.getRoundDate()));
            utils.LCUtil.logps(ps);

            try (ResultSet rs = ps.executeQuery()) {
                int i = 0;
                String json = null;
                while (rs.next()) {
                    i++;
                    json = rs.getString("TarifJson");
                }
                if (i == 0) {
                    String msg = LCUtil.prepareMessageBean("tarif.greenfee.notfound") + round.getCourseIdcourse();
                    LOG.warn(msg);
                    LCUtil.showMessageFatal(msg);
                    return null;
                } else {
                    LOG.debug(methodName + " - ResultSet has " + i + " lines.");
                }

                ObjectMapper om = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
                om.registerModule(new JavaTimeModule());
                om.configure(SerializationFeature.INDENT_OUTPUT, true);
                LOG.debug("Tarif Greenfee format json = " + json);
                TarifGreenfee tarifGreenfee = om.readValue(json, TarifGreenfee.class);
                LOG.debug("Tarif Greenfee extracted from database = " + tarifGreenfee);
                return tarifGreenfee;
            }

        } catch (SQLException e) {
            handleSQLException(e, methodName);
            return null;
        } catch (Exception e) {
            handleGenericException(e, methodName);
            return null;
        }
    } // end method

    /** @deprecated use {@link #find(Round)} via CDI injection */
    /*
    void main() throws SQLException, Exception {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName);
        Round round = new Round();
        round.setIdround(755);
        TarifGreenfee tarifGreenfee = find(round);
        LOG.debug("TarifGreenfee in main = " + tarifGreenfee);
    } // end main
    */

} // end class

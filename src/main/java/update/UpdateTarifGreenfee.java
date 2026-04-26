package update;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import entite.TarifGreenfee;
import static exceptions.LCException.handleGenericException;
import static exceptions.LCException.handleSQLException;
import static interfaces.Log.LOG;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.temporal.ChronoUnit;
import utils.LCUtil;

@ApplicationScoped
public class UpdateTarifGreenfee implements Serializable {

    private static final long serialVersionUID = 1L;

    private static final ObjectMapper OBJECT_MAPPER;

    static {
        OBJECT_MAPPER = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
        OBJECT_MAPPER.registerModule(new JavaTimeModule());
        OBJECT_MAPPER.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
    }

    @Inject private dao.GenericDAO dao;

    public UpdateTarifGreenfee() { }

    public boolean update(final TarifGreenfee tarif) throws SQLException {
        final String methodName = LCUtil.getCurrentMethodName();
        LOG.debug("entering {} - tarifId={}", methodName, tarif.getTarifId());

        if (tarif.getTarifId() == null) {
            LOG.error("tarifId is null — cannot UPDATE");
            LCUtil.showMessageFatal("UpdateTarifGreenfee: tarifId is null");
            return false;
        }
        if (tarif.getDatesSeasonsList().isEmpty()) {
            LOG.error("datesSeasonsList is empty");
            LCUtil.showMessageFatal("UpdateTarifGreenfee: datesSeasonsList is empty");
            return false;
        }

        final String query = """
            UPDATE tarif_greenfee
               SET TarifJson      = ?,
                   TarifYear      = ?,
                   TarifStartDate = ?,
                   TarifEndDate   = ?
             WHERE TarifId = ?
            """;

        try (Connection conn = dao.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {

            String json = OBJECT_MAPPER.writeValueAsString(tarif);
            LOG.debug("json length = {}", json.length());

            var lddeb = tarif.getDatesSeasonsList().get(0).getStartDate().truncatedTo(ChronoUnit.DAYS);
            int last  = tarif.getDatesSeasonsList().size() - 1;
            var ldfin = tarif.getDatesSeasonsList().get(last).getEndDate().truncatedTo(ChronoUnit.DAYS);

            sql.preparedstatement.psUpdateTarifGreenfee.psMapUpdate(ps, json, lddeb.getYear(), lddeb, ldfin, tarif.getTarifId());
            LCUtil.logps(ps);

            int rows = ps.executeUpdate();
            LOG.debug("rows updated = {}", rows);
            if (rows == 0) {
                LOG.error("no row updated for tarifId = {}", tarif.getTarifId());
                LCUtil.showMessageFatal("UpdateTarifGreenfee: no row updated for tarifId = " + tarif.getTarifId());
                return false;
            }

            String msg = LCUtil.prepareMessageBean("tarif.greenfee.updated");
            LOG.debug("tarif greenfee updated id = {}", tarif.getTarifId());
            LCUtil.showMessageInfo(msg);
            return true;

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
        LOG.debug("entering {}", methodName);
    } // end main
    */

} // end class

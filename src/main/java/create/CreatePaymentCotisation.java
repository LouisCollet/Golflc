package create;

import entite.Cotisation;
import static exceptions.LCException.handleGenericException;
import static exceptions.LCException.handleSQLException;
import static interfaces.Log.LOG;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import static interfaces.GolfInterface.ZDF_DAY;
import utils.LCUtil;

@ApplicationScoped
public class CreatePaymentCotisation implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject private dao.GenericDAO dao;
    @Inject private find.FindCotisationOverlapping findCotisationOverlapping;

    public CreatePaymentCotisation() { }

    public boolean create(final Cotisation cotisation) throws Exception {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        LOG.debug("with cotisation = {}", cotisation);

        if (findCotisationOverlapping.find(cotisation)) {
            String period = ZDF_DAY.format(cotisation.getCotisationStartDate())
                    + " - " + ZDF_DAY.format(cotisation.getCotisationEndDate());
            String label = LCUtil.prepareMessageBean("tarif.overlapping");
            String msg = "[COTISATION] " + (label != null ? label : "Cotisation overlap:") + " " + period;
            LOG.warn("overlap rejected player={} period={}", cotisation.getIdplayer(), period);
            throw new Exception(msg);
        }

        try (Connection conn = dao.getConnection()) {
            final String query = LCUtil.generateInsertQuery(conn, "payments_cotisation");
            try (PreparedStatement ps = conn.prepareStatement(query)) {
                PreparedStatement mapped = sql.preparedstatement.psCreatePaymentCotisation.psMapCreate(ps, cotisation);
                if (mapped == null) {
                    LOG.error("psMapCreate null — cotisation: idclub={} idplayer={} startDate={} endDate={} paymentRef={} status={}",
                        cotisation.getIdclub(), cotisation.getIdplayer(),
                        cotisation.getCotisationStartDate(), cotisation.getCotisationEndDate(),
                        cotisation.getPaymentReference(), cotisation.getStatus());
                    return false;
                }
                int row = ps.executeUpdate();
                if (row != 0) {
                    LOG.debug("cotisation payment created = {}", cotisation);
                    return true;
                } else {
                    LOG.error("[COTISATION] insert payments_cotisation returned 0 rows");
                    LCUtil.showMessageFatal("[COTISATION] " + LCUtil.prepareMessageBean("create.cotisation.error"));
                    return false;
                }
            }
        } catch (SQLException sqle) {
            if (sqle.getSQLState().equals("23000") && sqle.getErrorCode() == 1062) {
                String msg = "[COTISATION] Duplicate cotisation — already registered for player="
                    + cotisation.getIdplayer() + " club=" + cotisation.getIdclub();
                LOG.error(msg);
                throw new Exception(msg, sqle);
            }
            String msg = "[COTISATION] SQL error: " + sqle.getMessage()
                + " (state=" + sqle.getSQLState() + " code=" + sqle.getErrorCode() + ")";
            LOG.error(msg);
            throw new Exception(msg, sqle);
        } catch (Exception e) {
            handleGenericException(e, methodName);
            throw e;
        }
    } // end method

    /*
    void main() {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
    } // end main
    */

} // end class

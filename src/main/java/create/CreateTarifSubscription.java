package create;

import entite.TarifSubscription;
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
import utils.LCUtil;

@ApplicationScoped
public class CreateTarifSubscription implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject private dao.GenericDAO dao;

    @Inject
    private find.FindTarifSubscriptionOverlapping findTarifSubscriptionOverlapping;

    public CreateTarifSubscription() { }

    public boolean create(final TarifSubscription tarif) throws SQLException {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        LOG.debug("with tarif = {}", tarif);

        if (findTarifSubscriptionOverlapping.find(tarif)) {
            return false; // rejected for dates overlapping
        }

        try (Connection conn = dao.getConnection();
             PreparedStatement ps = conn.prepareStatement(utils.LCUtil.generateInsertQuery(conn, "tarif_subscription"))) {

            ps.setNull(1, java.sql.Types.INTEGER);                // TarifSubscriptionId auto-increment
            ps.setString(2, tarif.getCode());
            ps.setDouble(3, tarif.getPrice());
            ps.setTimestamp(4, Timestamp.valueOf(tarif.getStartDate()));
            ps.setTimestamp(5, Timestamp.valueOf(tarif.getEndDate()));
            ps.setTimestamp(6, Timestamp.from(Instant.now()));
            LCUtil.logps(ps);

            int row = ps.executeUpdate();
            if (row != 0) {
                String msg = "Tarif Subscription created = " + tarif;
                LOG.info(msg);
                LCUtil.showMessageInfo(msg);
                return true;
            } else {
                String msg = "Tarif Subscription NOT created = " + tarif;
                LOG.error(msg);
                LCUtil.showMessageFatal(msg);
                return false;
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
    void main() throws SQLException {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
    } // end main
    */

} // end class

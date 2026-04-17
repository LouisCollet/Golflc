package delete;

import entite.TarifSubscription;
import static exceptions.LCException.handleGenericException;
import static exceptions.LCException.handleSQLException;
import static interfaces.Log.LOG;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.io.Serializable;
import java.sql.SQLException;
import utils.LCUtil;

@ApplicationScoped
public class DeleteTarifSubscription implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject private dao.GenericDAO dao;

    public DeleteTarifSubscription() { }

    public boolean delete(final TarifSubscription tarif) throws SQLException {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        LOG.debug("with tarif = {}", tarif);

        final String query = """
                DELETE FROM tarif_subscription
                WHERE TarifSubscriptionCode = ?
                """;

        int rowDeleted = dao.execute(query, tarif.getCode());
        String msg = "There are " + rowDeleted + " TarifSubscription deleted for code = " + tarif.getCode();
        if (rowDeleted != 0) {
            LOG.info(msg);
            LCUtil.showMessageInfo(msg);
            return true;
        } else {
            LOG.error(msg);
            LCUtil.showMessageFatal(msg);
            return false;
        }
    } // end method

    public boolean deleteAll() throws SQLException {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);

        final String query = "DELETE FROM tarif_subscription";

        int rowDeleted = dao.execute(query);
        String msg = "There are " + rowDeleted + " TarifSubscription deleted (all)";
        if (rowDeleted != 0) {
            LOG.info(msg);
            LCUtil.showMessageInfo(msg);
            return true;
        } else {
            LOG.error(msg);
            LCUtil.showMessageFatal(msg);
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

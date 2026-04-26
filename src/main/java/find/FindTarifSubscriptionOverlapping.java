package find;

import entite.TarifSubscription;
import static exceptions.LCException.handleGenericException;
import static exceptions.LCException.handleSQLException;
import static interfaces.Log.LOG;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.io.Serializable;
import java.sql.SQLException;

@ApplicationScoped
public class FindTarifSubscriptionOverlapping implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject private find.OverlapChecker overlapChecker;

    public FindTarifSubscriptionOverlapping() { }

    public boolean find(final TarifSubscription tarif) throws SQLException {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        LOG.debug("for tarifSubscription = {}", tarif);
        try {
            return overlapChecker.check(
                    tarif.getStartDate(), tarif.getEndDate(),
                    """
                    SELECT TarifSubscriptionId, TarifSubscriptionCode, TarifSubscriptionPrice,
                           TarifSubscriptionStartDate, TarifSubscriptionEndDate,
                           TarifSubscriptionCreationDate
                    FROM tarif_subscription
                    WHERE TarifSubscriptionCode = ?
                    """,
                    ps -> ps.setString(1, tarif.getCode()),
                    new rowmappers.TarifSubscriptionRowMapper(),
                    TarifSubscription::getStartDate,
                    TarifSubscription::getEndDate);
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

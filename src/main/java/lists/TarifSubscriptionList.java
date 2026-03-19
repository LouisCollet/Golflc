package lists;

import entite.TarifSubscription;
import static interfaces.Log.LOG;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.Serializable;
import java.sql.SQLException;
import java.util.List;
import rowmappers.TarifSubscriptionRowMapper;

@Named
@ApplicationScoped
public class TarifSubscriptionList implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject private dao.GenericDAO dao;

    private List<TarifSubscription> liste = null;

    public TarifSubscriptionList() { }

    public List<TarifSubscription> list() throws SQLException {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName);

        if (liste != null) {
            LOG.debug(methodName + " - returning cached list size = " + liste.size());
            return liste;
        }

        final String query = """
                SELECT TarifSubscriptionId, TarifSubscriptionCode, TarifSubscriptionPrice,
                       TarifSubscriptionStartDate, TarifSubscriptionEndDate,
                       TarifSubscriptionCreationDate
                FROM tarif_subscription
                ORDER BY TarifSubscriptionCode, TarifSubscriptionStartDate
                """;

        liste = dao.queryList(query, new TarifSubscriptionRowMapper());
        return liste;
    } // end method

    public List<TarifSubscription> getListe() { return liste; }
    public void setListe(List<TarifSubscription> liste) { this.liste = liste; }

    public void invalidateCache() {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName);
        this.liste = null;
        LOG.debug(methodName + " - cache invalidated");
    } // end method

    /*
    void main() throws SQLException {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName);
    } // end main
    */

} // end class

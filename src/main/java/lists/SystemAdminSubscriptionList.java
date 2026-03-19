package lists;

import entite.composite.ECourseList;
import static interfaces.Log.LOG;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.Serializable;
import java.sql.SQLException;
import java.util.List;
import rowmappers.PlayerRowMapper;
import rowmappers.RowMapper;
import rowmappers.SubscriptionRowMapper;

@Named("SASubscription")
@ViewScoped // nécessaire !! pour faire le total dans local_administrator_cotisations.xhtml
public class SystemAdminSubscriptionList implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject private dao.GenericDAO dao;

    // ✅ Cache d'instance — @ViewScoped resets per view automatically
    private List<ECourseList> liste = null;

    public SystemAdminSubscriptionList() { }

    public List<ECourseList> list() throws SQLException {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName);

        // ✅ Early return — guard clause FIRST
        if (liste != null) {
            LOG.debug(methodName + " - returning cached list size = " + liste.size());
            return liste;
        }

        final String query = """
            SELECT *
            FROM payments_subscription, player
            WHERE player.idplayer = payments_subscription.SubscriptionIdPLayer
            ORDER BY SubscriptionStartDate
            """;

        RowMapper<ECourseList> compositeMapper = rs -> ECourseList.builder()
                .player(new PlayerRowMapper().map(rs))
                .subscription(new SubscriptionRowMapper().map(rs))
                .build();

        liste = dao.queryList(query, compositeMapper);
        return liste;
    } // end method

    // ✅ Getters/setters d'instance
    public List<ECourseList> getListe()              { return liste; }
    public void setListe(List<ECourseList> liste)    { this.liste = liste; }

    // ✅ Invalidation explicite
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
        var lp = new SystemAdminSubscriptionList().list();
        LOG.debug("from main, result = " + lp);
    } // end main
    */

} // end class

package lists;

import entite.composite.ECourseList;
import static exceptions.LCException.handleGenericException;
import static interfaces.Log.LOG;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.io.Serializable;
import java.sql.SQLException;
import java.util.List;
import rowmappers.PlayerRowMapper;
import rowmappers.RowMapper;
import rowmappers.SubscriptionRowMapper;

// non testé
@ApplicationScoped
public class SubscriptionRenewalList implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject private dao.GenericDAO dao;

    @Inject private mail.SubscriptionMail subscriptionMail;  // migrated 2026-02-26

    // ✅ Cache d'instance — @ApplicationScoped garantit le singleton
    private List<ECourseList> liste = null;

    public SubscriptionRenewalList() { }

    /*/ ✅ Constante partagée avec le test d'intégration — modifier ici suffit
    public static final String QUERY = """
            SELECT *
            FROM payments_subscription
            JOIN player
               ON player.idplayer = payments_subscription.SubscriptionIdPlayer
               AND PlayerActivation = '1'
            WHERE YEAR(SubscriptionEndDate)  = YEAR(DATE_ADD(CURRENT_DATE(), INTERVAL 1 MONTH))
              AND MONTH(SubscriptionEndDate) = MONTH(DATE_ADD(CURRENT_DATE(), INTERVAL 1 MONTH))
            """;
*/
     public static final String QUERY = """
    SELECT ps.*, p.*
    FROM payments_subscription ps
    JOIN player p
      ON p.idplayer = ps.SubscriptionIdPlayer
    WHERE p.PlayerActivation = 1
      AND ps.SubscriptionEndDate >= DATE_ADD(DATE_SUB(CURDATE(), INTERVAL DAY(CURDATE())-1 DAY), INTERVAL 1 MONTH)
      AND ps.SubscriptionEndDate <  DATE_ADD(DATE_SUB(CURDATE(), INTERVAL DAY(CURDATE())-1 DAY), INTERVAL 2 MONTH);
    """;
    
     public List<ECourseList> list() throws SQLException {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);

        // ✅ Early return — guard clause FIRST
        if (liste != null) {
            LOG.debug(methodName + " - returning cached list size = " + liste.size());
            return liste;
        }

        RowMapper<ECourseList> compositeMapper = rs -> ECourseList.builder()
                .player(new PlayerRowMapper().map(rs))
                .subscription(new SubscriptionRowMapper().map(rs))
                .build();

        liste = dao.queryList(QUERY, compositeMapper);

        liste.forEach(item -> LOG.debug("players candidates to renewal = " + item));

        // partie 2 — envoi des mails de renouvellement
        for (ECourseList item : liste) {
            try {
                LOG.debug("Player we send a Subscription Renewal mail = " + item.player().getPlayerLastName());
                subscriptionMail.sendMail(item.player(), item.subscription()); // migrated 2026-02-26
            } catch (Exception e) {
                final String methodName2 = utils.LCUtil.getCurrentMethodName();
                handleGenericException(e, methodName2);
            }
        } // end for

        return liste;
    } // end method

    // ✅ Getters/setters d'instance
    public List<ECourseList> getListe()              { return liste; }
    public void setListe(List<ECourseList> liste)    { this.liste = liste; }

    // ✅ Invalidation explicite
    public void invalidateCache() {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        this.liste = null;
        LOG.debug(methodName + " - cache invalidated");
    } // end method

    /*
    void main() throws SQLException {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        List<ECourseList> ec = new SubscriptionRenewalList().list();
        LOG.debug("from main, ec = " + ec);
    } // end main
    */

} // end class

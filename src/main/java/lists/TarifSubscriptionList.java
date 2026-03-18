package lists;

import entite.TarifSubscription;
import static exceptions.LCException.handleGenericException;
import static exceptions.LCException.handleSQLException;
import static interfaces.Log.LOG;
import jakarta.annotation.Resource;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Named;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.sql.DataSource;
import rowmappers.RowMapper;
import rowmappers.TarifSubscriptionRowMapper;

@Named
@ApplicationScoped
public class TarifSubscriptionList implements Serializable {

    private static final long serialVersionUID = 1L;

    @Resource(lookup = "java:jboss/datasources/golflc")
    private DataSource dataSource;

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

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {

            utils.LCUtil.logps(ps);

            try (ResultSet rs = ps.executeQuery()) {
                liste = new ArrayList<>();
                RowMapper<TarifSubscription> mapper = new TarifSubscriptionRowMapper();
                while (rs.next()) {
                    liste.add(mapper.map(rs));
                }
                if (liste.isEmpty()) {
                    LOG.warn(methodName + " - empty result list");
                } else {
                    LOG.debug(methodName + " - list size = " + liste.size());
                }
                return liste;
            }

        } catch (SQLException e) {
            handleSQLException(e, methodName);
            return Collections.emptyList();
        } catch (Exception e) {
            handleGenericException(e, methodName);
            return Collections.emptyList();
        }
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

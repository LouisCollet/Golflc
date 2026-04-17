package find;

import entite.TarifSubscription;
import static exceptions.LCException.handleGenericException;
import static exceptions.LCException.handleSQLException;
import static interfaces.Log.LOG;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import rowmappers.TarifSubscriptionRowMapper;

@ApplicationScoped
public class FindTarifSubscription implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject private dao.GenericDAO dao;

    public FindTarifSubscription() { }

    public TarifSubscription findActive(final String code) throws SQLException {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        LOG.debug("with code = " + code);

        final String query = """
                SELECT TarifSubscriptionId, TarifSubscriptionCode, TarifSubscriptionPrice,
                       TarifSubscriptionStartDate, TarifSubscriptionEndDate,
                       TarifSubscriptionCreationDate
                FROM tarif_subscription
                WHERE TarifSubscriptionCode = ?
                AND NOW() BETWEEN TarifSubscriptionStartDate AND TarifSubscriptionEndDate
                ORDER BY TarifSubscriptionStartDate DESC
                LIMIT 1
                """;

        try (Connection conn = dao.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setString(1, code);
            utils.LCUtil.logps(ps);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    TarifSubscription t = new TarifSubscriptionRowMapper().map(rs);
                    LOG.debug(methodName + " - found active tarif = " + t);
                    return t;
                }
                LOG.debug(methodName + " - no active tarif found for code = " + code);
                return null;
            }

        } catch (SQLException e) {
            handleSQLException(e, methodName);
            return null;
        } catch (Exception e) {
            handleGenericException(e, methodName);
            return null;
        }
    } // end method

    /*
    void main() throws SQLException {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
    } // end main
    */

} // end class

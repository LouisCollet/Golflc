package find;

import com.github.mawippel.validator.OverlappingVerificator;
import entite.TarifSubscription;
import static exceptions.LCException.handleGenericException;
import static exceptions.LCException.handleSQLException;
import static interfaces.GolfInterface.ZDF_DAY;
import static interfaces.Log.LOG;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import rowmappers.RowMapper;
import rowmappers.TarifSubscriptionRowMapper;
import utils.LCUtil;
import static utils.LCUtil.showMessageFatal;

@ApplicationScoped
public class FindTarifSubscriptionOverlapping implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject private dao.GenericDAO dao;

    public FindTarifSubscriptionOverlapping() { }

    /**
     * Checks if the new tarif subscription overlaps with existing ones for the same code.
     * @param tarifNew the new tarif to check
     * @return true if overlap detected (= rejected), false if no overlap (= OK to insert)
     */
    public boolean find(final TarifSubscription tarifNew) throws SQLException {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        LOG.debug("for tarifSubscription = " + tarifNew);

        final String query = """
                SELECT TarifSubscriptionId, TarifSubscriptionCode, TarifSubscriptionPrice,
                       TarifSubscriptionStartDate, TarifSubscriptionEndDate,
                       TarifSubscriptionCreationDate
                FROM tarif_subscription
                WHERE TarifSubscriptionCode = ?
                """;

        try (Connection conn = dao.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setString(1, tarifNew.getCode());
            utils.LCUtil.logps(ps);

            try (ResultSet rs = ps.executeQuery()) {
                List<TarifSubscription> liste = new ArrayList<>();
                RowMapper<TarifSubscription> mapper = new TarifSubscriptionRowMapper();
                while (rs.next()) {
                    liste.add(mapper.map(rs));
                }
                LOG.debug(methodName + " - found " + liste.size() + " existing tarifs for code = " + tarifNew.getCode());

                if (liste.isEmpty()) {
                    LOG.debug(methodName + " - no overlap, first tarif for this code");
                    return false;
                }

                for (TarifSubscription existing : liste) {
                    boolean isOverlap = OverlappingVerificator.isOverlap(
                            tarifNew.getStartDate(), tarifNew.getEndDate(),
                            existing.getStartDate(), existing.getEndDate());
                    LOG.debug(methodName + " - isOverlap ? = " + isOverlap);
                    if (isOverlap) {
                        String msg = LCUtil.prepareMessageBean("tarif.overlapping")
                                + ZDF_DAY.format(tarifNew.getStartDate()) + " - " + ZDF_DAY.format(tarifNew.getEndDate())
                                + " against <br>"
                                + ZDF_DAY.format(existing.getStartDate()) + " - " + ZDF_DAY.format(existing.getEndDate());
                        LOG.error(msg);
                        showMessageFatal(msg);
                        return true;
                    }
                } // end for
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

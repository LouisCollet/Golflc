package update;

import entite.UnavailablePeriod;
import static exceptions.LCException.handleGenericException;
import static exceptions.LCException.handleSQLException;
import static interfaces.Log.LOG;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import utils.LCUtil;

@ApplicationScoped
public class UpdateUnavailablePeriod implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject private dao.GenericDAO dao;

    public UpdateUnavailablePeriod() { }

    public boolean updateAvailability(final UnavailablePeriod period) throws SQLException {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);

        final String query = """
            UPDATE unavailable_periods
            SET UnavailableItems = ?,
                UnavailableStartDate = ?,
                UnavailableEndDate = ?,
                UnavailableModificationDate = ?
            WHERE UnavailableIdClub = ?
            ORDER BY UnavailableModificationDate DESC
            LIMIT 1
            """;

        try (Connection conn = dao.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {

            sql.preparedstatement.psCreateUpdateUnavailablePeriod.psMapUpdate(ps, period);

            int row = ps.executeUpdate();
            LOG.debug("rows updated = {}", row);
            return row > 0;

        } catch (SQLException e) {
            handleSQLException(e, methodName);
            return false;
        } catch (Exception e) {
            handleGenericException(e, methodName);
            return false;
        }
    } // end method

} // end class

package find;

import entite.UnavailablePeriod;
import static exceptions.LCException.handleGenericException;
import static exceptions.LCException.handleSQLException;
import static interfaces.Log.LOG;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.io.Serializable;
import java.sql.SQLException;

@ApplicationScoped
public class FindUnavailablePeriodOverlapping implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject private find.OverlapChecker overlapChecker;

    public FindUnavailablePeriodOverlapping() { }

    public boolean find(final UnavailablePeriod period) throws SQLException {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        LOG.debug("for period = {}", period);
        try {
            return overlapChecker.check(
                    period.getStartDate(), period.getEndDate(),
                    "SELECT * FROM unavailable_periods WHERE UnavailableIdClub = ?",
                    ps -> ps.setInt(1, period.getIdclub()),
                    new rowmappers.UnavailablePeriodRowMapper(),
                    UnavailablePeriod::getStartDate,
                    UnavailablePeriod::getEndDate);
        } catch (SQLException e) {
            handleSQLException(e, methodName);
            return false;
        } catch (Exception e) {
            handleGenericException(e, methodName);
            return false;
        }
    } // end method

} // end class

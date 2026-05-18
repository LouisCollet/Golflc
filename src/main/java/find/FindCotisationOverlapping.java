package find;

import entite.Cotisation;
import static exceptions.LCException.handleGenericException;
import static exceptions.LCException.handleSQLException;
import static interfaces.Log.LOG;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.io.Serializable;
import java.sql.SQLException;

@ApplicationScoped
public class FindCotisationOverlapping implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject private find.OverlapChecker overlapChecker;

    public FindCotisationOverlapping() { }

    public boolean find(final Cotisation cotisation) throws SQLException {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        LOG.debug("for cotisation = {}", cotisation);
        try {
            return overlapChecker.check(
                    "[COTISATION]",
                    cotisation.getCotisationStartDate(),
                    cotisation.getCotisationEndDate(),
                    """
                    SELECT * FROM payments_cotisation
                    WHERE CotisationIdPlayer = ?
                      AND CotisationIdClub   = ?
                      AND CotisationStatus   = 'Y'
                    """,
                    ps -> {
                        ps.setInt(1, cotisation.getIdplayer());
                        ps.setInt(2, cotisation.getIdclub());
                    },
                    new rowmappers.CotisationRowMapper(),
                    Cotisation::getCotisationStartDate,
                    Cotisation::getCotisationEndDate);
        } catch (SQLException e) {
            handleSQLException(e, methodName);
            return false;
        } catch (Exception e) {
            handleGenericException(e, methodName);
            return false;
        }
    } // end method

    /*
    void main() {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
    } // end main
    */

} // end class

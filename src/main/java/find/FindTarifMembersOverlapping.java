package find;

import entite.TarifMember;
import static exceptions.LCException.handleGenericException;
import static exceptions.LCException.handleSQLException;
import static interfaces.Log.LOG;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.io.Serializable;
import java.sql.SQLException;

@ApplicationScoped
public class FindTarifMembersOverlapping implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject private find.OverlapChecker overlapChecker;

    public FindTarifMembersOverlapping() { }

    public boolean find(final TarifMember tarif) throws SQLException {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        LOG.debug("for tarifMember = {}", tarif);
        try {
            return overlapChecker.check(
                    tarif.getStartDate(), tarif.getEndDate(),
                    "SELECT * FROM tarif_members WHERE TarifMemberIdClub = ?",
                    ps -> ps.setInt(1, tarif.getTarifMemberIdClub()),
                    new rowmappers.TarifMemberRowMapper(),
                    TarifMember::getStartDate,
                    TarifMember::getEndDate);
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
        TarifMember tm = new TarifMember();
        tm.setStartDate(java.time.LocalDateTime.of(2021, java.time.Month.JANUARY, 1, 0, 0));
        tm.setEndDate(java.time.LocalDateTime.of(2021, java.time.Month.DECEMBER, 31, 0, 0));
        tm.setTarifMemberIdClub(1122);
        boolean b = find(tm);
        LOG.debug("result overlapping = {}", b);
    } // end main
    */

} // end class

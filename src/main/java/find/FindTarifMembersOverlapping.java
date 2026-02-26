package find;

import com.github.mawippel.validator.OverlappingVerificator;
import entite.TarifMember;
import static exceptions.LCException.handleGenericException;
import static exceptions.LCException.handleSQLException;
import static interfaces.GolfInterface.ZDF_DAY;
import static interfaces.Log.LOG;
import jakarta.annotation.Resource;
import jakarta.enterprise.context.ApplicationScoped;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.sql.DataSource;
import utils.LCUtil;
import static utils.LCUtil.showMessageFatal;

@ApplicationScoped
public class FindTarifMembersOverlapping implements Serializable {

    private static final long serialVersionUID = 1L;

    @Resource(lookup = "java:jboss/datasources/golflc")
    private DataSource dataSource;

    private List<TarifMember> liste = null;

    public FindTarifMembersOverlapping() { }

    public boolean find(final TarifMember tarif_new) throws SQLException {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName);
        LOG.debug("for tarifMember = " + tarif_new);

        final String query = """
                SELECT *
                FROM tarif_members
                WHERE tarif_members.TarifMemberIdClub = ?
                """;

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, tarif_new.getTarifMemberIdClub());
            utils.LCUtil.logps(ps);
            try (ResultSet rs = ps.executeQuery()) {
                int i = 0;
                liste = new ArrayList<>();
                while (rs.next()) {
                    liste.add(entite.TarifMember.map(rs));
                    i++;
                }
                LOG.debug("ResultSet " + methodName + " has " + i + " lines.");
                if (i == 0) {
                    LOG.debug("ok because i = 0 : first tarif for this club !");
                    return false;
                }
                liste.forEach(item -> LOG.debug("list of TarifMember =" + item));
                for (i = 0; i < liste.size(); i++) {
                    boolean isOverlap = OverlappingVerificator.isOverlap(
                            tarif_new.getStartDate(), tarif_new.getEndDate(),
                            liste.get(i).getStartDate(), liste.get(i).getEndDate());
                    LOG.debug(" isOverlap ? = " + isOverlap);
                    if (isOverlap) {
                        String msg = LCUtil.prepareMessageBean("tarif.overlapping")
                                + ZDF_DAY.format(tarif_new.getStartDate()) + " - " + ZDF_DAY.format(tarif_new.getEndDate())
                                + " against <br>"
                                + ZDF_DAY.format(liste.get(i).getStartDate()) + " - " + ZDF_DAY.format(liste.get(i).getEndDate());
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
    void main() {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName);
        TarifMember tm = new TarifMember();
        tm.setStartDate(LocalDateTime.of(2021, Month.JANUARY, 1, 0, 0));
        tm.setEndDate(LocalDateTime.of(2021, Month.DECEMBER, 31, 0, 0));
        tm.setTarifMemberIdClub(1122);
        boolean b = new FindTarifMembersOverlapping().find(tm);
        LOG.debug("result overlapping = " + b);
    } // end main
    */

} // end class

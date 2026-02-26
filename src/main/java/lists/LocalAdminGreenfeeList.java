package lists;

import entite.Club;
import entite.Greenfee;
import entite.Player;
import entite.composite.ECourseList;
import static exceptions.LCException.handleGenericException;
import static exceptions.LCException.handleSQLException;
import static interfaces.Log.LOG;
import jakarta.annotation.Resource;
import jakarta.faces.view.ViewScoped;
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
import rowmappers.ClubRowMapper;
import rowmappers.GreenfeeRowMapper;
import rowmappers.RowMapper;

@Named("LAGreenfee")
@ViewScoped // nécessaire !! pour faire le total dans local_administrator_greenfee.xhtml
public class LocalAdminGreenfeeList implements Serializable {

    private static final long serialVersionUID = 1L;

    @Resource(lookup = "java:jboss/datasources/golflc")
    private DataSource dataSource;

    // ✅ Cache d'instance — @ViewScoped resets per view automatically
    private List<ECourseList> liste = null;

    public LocalAdminGreenfeeList() { }

    public List<ECourseList> list(final Player localAdmin) throws SQLException {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName);

        // ✅ Early return — guard clause FIRST
        if (liste != null) {
            LOG.debug(methodName + " - returning cached list size = " + liste.size());
            return liste;
        }

        final String query = """
               /* lists.LocalAdminGreenfeeList.list  */
            SELECT *
            FROM payments_greenfee, club
            WHERE club.ClubLocalAdmin = ?
              AND payments_greenfee.GreenfeeIdClub = club.idclub
            GROUP BY idgreenfee
            ORDER BY GreenfeeIdClub
            """;

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setInt(1, localAdmin.getIdplayer());
            utils.LCUtil.logps(ps);

            try (ResultSet rs = ps.executeQuery()) {
                liste = new ArrayList<>();
                RowMapper<Club> clubMapper = new ClubRowMapper();
                RowMapper<Greenfee> greenfeeMapper = new GreenfeeRowMapper();

                while (rs.next()) {
                    ECourseList ecl = ECourseList.builder()
                            .club(clubMapper.map(rs))
                            .greenfee(greenfeeMapper.map(rs))
                            .build();
                    liste.add(ecl);
                }
                if (liste.isEmpty()) {
                    LOG.warn(methodName + " - empty result list");
                } else {
                    LOG.debug(methodName + " - list size = " + liste.size());
                    liste.forEach(item -> LOG.debug("Players list with greenfee paid = " + item.greenfee().getPrice()
                            + " /IdClub = " + item.club().getIdclub()));
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
        Player player = new Player();
        player.setIdplayer(324715);
        var lp = new LocalAdminGreenfeeList().list(player);
        LOG.debug("from main, result size = " + lp.size());
    } // end main
    */

} // end class

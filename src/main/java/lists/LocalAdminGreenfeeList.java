package lists;

import entite.Club;
import entite.Greenfee;
import entite.Player;
import entite.composite.ECourseList;
import static interfaces.Log.LOG;
import jakarta.inject.Inject;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;
import java.io.Serializable;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import rowmappers.ClubRowMapper;
import rowmappers.GreenfeeRowMapper;
import rowmappers.RowMapper;

@Named("LAGreenfee")
@ViewScoped // nécessaire !! pour faire le total dans local_administrator_greenfee.xhtml
public class LocalAdminGreenfeeList implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject private dao.GenericDAO dao;

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

        RowMapper<Club> clubMapper = new ClubRowMapper();
        RowMapper<Greenfee> greenfeeMapper = new GreenfeeRowMapper();

        liste = new ArrayList<>(dao.queryList(query, rs -> ECourseList.builder()
                .club(clubMapper.map(rs))
                .greenfee(greenfeeMapper.map(rs))
                .build(),
                localAdmin.getIdplayer()));

        if (liste.isEmpty()) {
            LOG.warn(methodName + " - empty result list");
        } else {
            LOG.debug(methodName + " - list size = " + liste.size());
            liste.forEach(item -> LOG.debug("Players list with greenfee paid = " + item.greenfee().getPrice()
                    + " /IdClub = " + item.club().getIdclub()));
        }
        return liste;
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

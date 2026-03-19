package lists;

import entite.Club;
import entite.Player;
import static interfaces.Log.LOG;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.Serializable;
import java.sql.SQLException;
import java.util.List;
import rowmappers.ClubRowMapper;

/**
 * fix multi-user 2026-03-07 — cache supprimé (données per-admin dans singleton = fuite de données)
 */
@Named
@ApplicationScoped
public class ClubsListLocalAdmin implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject private dao.GenericDAO dao;

    public ClubsListLocalAdmin() { }

    public List<Club> list(final Player localAdmin) throws SQLException {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName);

        final String query = """
                SELECT *
                FROM club
                WHERE club.ClubLocalAdmin = ?
                """;

        List<Club> result = dao.queryList(query, new ClubRowMapper(), localAdmin.getIdplayer());
        if (result.isEmpty()) {
            LOG.warn(methodName + " - empty result list");
        } else {
            LOG.debug(methodName + " - list size = " + result.size());
        }
        return result;
    } // end method

    /**
     * No-op — cache removed (fix multi-user 2026-03-07)
     */
    public void invalidateCache() {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName + " - no-op (cache removed)");
    } // end method

    /*
    void main() throws SQLException {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName);
        Player localAdmin = new Player();
        localAdmin.setIdplayer(324715);
        List<Club> lp = list(localAdmin);
        LOG.debug("from main, after lp = " + lp);
    } // end main
    */

} // end class

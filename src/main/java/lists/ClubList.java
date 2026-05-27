
package lists;

import entite.Club;
import static interfaces.Log.LOG;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.io.Serializable;
import java.sql.SQLException;
import java.util.List;
import rowmappers.ClubRowMapper;

@ApplicationScoped
public class ClubList implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject private dao.GenericDAO dao;

    // ✅ Cache d'instance — @ApplicationScoped garantit le singleton
    private List<Club> liste = null;

    /**
     * Liste tous les clubs
     * @return liste de tous les clubs
     */
    public List<Club> list() throws SQLException {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);

        // ✅ EARLY RETURN - Guard clause
        if (liste != null) {
            LOG.debug("escaped to " + methodName + " repetition thanks to lazy loading");
            return liste;
        }

        // Chargement depuis la base de données
        LOG.debug("entering " + methodName);

        final String query = """
            SELECT *
            FROM club
            """;

        liste = dao.queryList(query, new ClubRowMapper());

        if (liste.isEmpty()) {
            String msg = "Empty Result List ClubList in " + methodName;
            LOG.warn(msg);
        } else {
            LOG.debug("ResultSet " + methodName + " has " + liste.size() + " lines.");
        }

        return liste;
    } // end method

    // ✅ Getters/setters d'instance
    public List<Club> getListe()               { return liste; }
    public void setListe(List<Club> liste)     { this.liste = liste; }

    // ✅ Invalidation explicite
    public void invalidateCache() {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        this.liste = null;
        LOG.debug(methodName + " - cache invalidated");
    } // end method

    /*
    void main() throws SQLException {
        try {
            List<Club> lp = new ClubList().list();
            LOG.debug("from main, after lp = " + lp);
            LOG.debug("nombre de clubs dans la liste = " + lp.size());
        } catch (Exception e) {
            String msg = "Exception in main: " + e.getMessage();
            LOG.error(msg, e);
        }
    } // end main
    */

} // end class

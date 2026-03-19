package lists;

import entite.Player;
import entite.Professional;
import static interfaces.Log.LOG;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.io.Serializable;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import rowmappers.ProfessionalRowMapper;

// vérifier si un player est aussi un pro : retourner les liste des clubs où il est pro
@ApplicationScoped
public class ProfessionalClubList implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject private dao.GenericDAO dao;

    // ✅ Cache d'instance — @ApplicationScoped garantit le singleton
    private List<Professional> liste = null;

    public ProfessionalClubList() { }

    public List<Professional> list(final Player player) throws SQLException {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName);
        LOG.debug("with Player " + player);

        // ✅ Early return — guard clause FIRST
        if (liste != null) {
            LOG.debug(methodName + " - returning cached list size = " + liste.size());
            return liste;
        }

        final String query = """
            SELECT *
            FROM professional
            WHERE professional.ProPlayerId = ?
            AND DATE(NOW()) BETWEEN DATE(ProClubStartDate) AND DATE(ProClubEndDate)
            """;

        liste = new ArrayList<>(dao.queryList(query, new ProfessionalRowMapper(), player.getIdplayer()));

        if (liste.isEmpty()) {
            LOG.warn(methodName + " - empty result list");
        } else {
            LOG.debug(methodName + " - list size = " + liste.size());
        }
        return liste;
    } // end method

    // ✅ Getters/setters d'instance
    public List<Professional> getListe()              { return liste; }
    public void setListe(List<Professional> liste)    { this.liste = liste; }

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
        List<Professional> prof = new ProfessionalClubList().list(player);
        LOG.debug("schedule list for a Pro = " + prof.size());
        prof.forEach(item -> LOG.debug("Club(s) list for a Pro " + item));
    } // end main
    */

} // end class

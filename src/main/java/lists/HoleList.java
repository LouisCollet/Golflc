package lists;

import entite.Hole;
import static interfaces.Log.LOG;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.Serializable;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import rowmappers.HoleRowMapper;

@Named
@ApplicationScoped
public class HoleList implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject private dao.GenericDAO dao;

    // ✅ Cache d'instance — @ApplicationScoped garantit le singleton
    private List<Hole> liste = null;

    public HoleList() { }

    /**
     * Liste les holes pour un tee donné
     * @param teeId l'ID du tee
     * @return liste des holes (18 holes normalement)
     */
    public List<Hole> listForTee(final int teeId) throws SQLException {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName);
        LOG.debug(methodName + " - for teeId = " + teeId);

        if (liste != null) {
            LOG.debug(methodName + " - returning cached list size = " + liste.size());
            return liste;
        }

        final String query = """
                SELECT *
                FROM hole
                WHERE hole.tee_idtee = ?
                ORDER BY HoleNumber
                """;

        liste = new ArrayList<>(dao.queryList(query, new HoleRowMapper(), teeId));

        if (liste.isEmpty()) {
            LOG.warn(methodName + " - empty result list for teeId = " + teeId);
        } else {
            LOG.debug(methodName + " - list size = " + liste.size());
        }
        return liste;
    } // end method

    public List<Hole> getListe()                { return liste; }
    public void       setListe(List<Hole> liste) { this.liste = liste; }

    public void invalidateCache() {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName);
        this.liste = null;
        LOG.debug(methodName + " - cache invalidated");
    } // end method

    /*
    void main() {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName);
        // nécessite contexte CDI — DataSource injecté par WildFly
        int teeId = 98;
        List<Hole> holes = new HoleList().listForTee(teeId);
        LOG.debug("hole list for tee = " + holes.size());
        holes.forEach(hole -> LOG.debug("Hole: " + hole.getHoleNumber() + " - Par: " + hole.getHolePar()));
    } // end main
    */

} // end class

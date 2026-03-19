package lists;

import entite.CompetitionDescription;
import static interfaces.Log.LOG;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.Serializable;
import java.sql.SQLException;
import java.util.List;

@Named
@ApplicationScoped
public class CompetitionDescriptionList implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject private dao.GenericDAO dao;

    private List<CompetitionDescription> liste = null;

    public CompetitionDescriptionList() { }

    public List<CompetitionDescription> list() throws SQLException {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName);

        if (liste != null) {
            LOG.debug(methodName + " - returning cached list size = " + liste.size());
            return liste;
        }

        final String query = """
            SELECT *
            FROM competition_description
            """;

        liste = dao.queryList(query, rs -> {
            try {
                return entite.CompetitionDescription.map(rs);
            } catch (SQLException e) {
                throw e;
            } catch (Exception e) {
                throw new SQLException("CompetitionDescription.map failed", e);
            }
        });
        return liste;
    } // end method

    public List<CompetitionDescription> getListe()                                    { return liste; }
    public void                         setListe(List<CompetitionDescription> liste)  { this.liste = liste; }

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
        // var lp = list();
        // LOG.debug("from main, list = " + lp);
        LOG.debug("from main, CompetitionDescriptionList = ");
    } // end main
    */

} // end class

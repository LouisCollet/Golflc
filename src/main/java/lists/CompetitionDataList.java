package lists;

import entite.CompetitionData;
import entite.CompetitionDescription;
import static interfaces.Log.LOG;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.Serializable;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@Named
@ApplicationScoped
public class CompetitionDataList implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject private dao.GenericDAO dao;

    private List<CompetitionData> liste = null;

    public CompetitionDataList() { }

    public List<CompetitionData> list(final CompetitionDescription cd) throws SQLException {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName);
        LOG.debug(methodName + " - for competition = " + cd);

        if (liste != null) {
            LOG.debug(methodName + " - returning cached list size = " + liste.size());
            return liste;
        }

        final String query = """
            SELECT *
            FROM competition_data
            WHERE CmpDataCompetitionId = ?
            ORDER BY CmpDataFlightNumber
            """;

        liste = new ArrayList<>(dao.queryList(query,
                rs -> entite.CompetitionData.map(rs),
                cd.getCompetitionId()));

        if (liste.isEmpty()) {
            LOG.warn(methodName + " - empty result list");
        } else {
            LOG.debug(methodName + " - list size = " + liste.size());
        }
        return liste;
    } // end method

    public List<CompetitionData> getListe()                          { return liste; }
    public void                  setListe(List<CompetitionData> liste) { this.liste = liste; }

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
        // CompetitionDescription cde = new CompetitionDescription();
        // cde.setCompetitionId(24);
        // var lp = list(cde);
        // LOG.debug("from main, list = " + lp);
        LOG.debug("from main, CompetitionDataList = ");
    } // end main
    */

} // end class

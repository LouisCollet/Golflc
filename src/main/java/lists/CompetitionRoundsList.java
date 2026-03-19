package lists;

import entite.CompetitionDescription;
import entite.composite.ECompetition;
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
public class CompetitionRoundsList implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject private dao.GenericDAO dao;

    private List<ECompetition> liste = null;

    public CompetitionRoundsList() { }

    public List<ECompetition> list(final CompetitionDescription cd) throws SQLException {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName);
        LOG.debug(methodName + " - for competition = " + cd);
        LOG.debug(methodName + " - for competitionId = " + cd.getCompetitionId());

        if (liste != null) {
            LOG.debug(methodName + " - returning cached list size = " + liste.size());
            return liste;
        }

        final String query = """
            SELECT *
            FROM competition_data
            JOIN competition_description
                ON cmpDataCompetitionId = CompetitionId
            WHERE CompetitionId = ?
            ORDER BY CmpDataFlightNumber
            """;

        liste = new ArrayList<>(dao.queryList(query, rs -> {
            try {
                var description = entite.CompetitionDescription.map(rs);
                var data = entite.CompetitionData.map(rs);
                return new ECompetition(description, data);
            } catch (Exception e) {
                throw new java.sql.SQLException(e);
            }
        }, cd.getCompetitionId()));

        if (liste.isEmpty()) {
            LOG.warn(methodName + " - empty result list");
        } else {
            LOG.debug(methodName + " - list size = " + liste.size());
        }
        return liste;
    } // end method

    public List<ECompetition> getListe()                          { return liste; }
    public void               setListe(List<ECompetition> liste)  { this.liste = liste; }

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
        // CompetitionDescription cd = new CompetitionDescription();
        // cd.setCompetitionId(35);
        // var lp = list(cd);
        // LOG.debug("from main, list = " + lp);
        LOG.debug("from main, CompetitionRoundsList = ");
    } // end main
    */

} // end class

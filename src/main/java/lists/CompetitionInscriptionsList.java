package lists;

import entite.CompetitionDescription;
import entite.composite.ECompetition;
import entite.HandicapIndex;
import static interfaces.Log.LOG;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.Serializable;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Named
@ApplicationScoped
public class CompetitionInscriptionsList implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject private dao.GenericDAO dao;

    @Inject private find.FindHandicapIndexAtDate findHandicapIndexAtDate;

    private List<ECompetition> liste = null;

    public CompetitionInscriptionsList() { }

    public List<ECompetition> list(final CompetitionDescription cd) throws SQLException {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName);
        LOG.debug(methodName + " - for competition description = " + cd);

        if (cd == null) {
            LOG.warn(methodName + " - cd is null, returning empty list");
            return Collections.emptyList();
        }

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

        List<ECompetition> rows = dao.queryList(query, rs -> {
            try {
                var description = entite.CompetitionDescription.map(rs);
                var data = entite.CompetitionData.map(rs);
                return new ECompetition(description, data);
            } catch (Exception e) {
                throw new java.sql.SQLException(e);
            }
        }, cd.getCompetitionId());

        liste = new ArrayList<>();
        for (ECompetition ec : rows) {
            HandicapIndex handicapIndex = new HandicapIndex();
            handicapIndex.setHandicapPlayerId(ec.competitionData().getCmpDataPlayerId());
            handicapIndex.setHandicapDate(ec.competitionDescription().getCompetitionDate());
            handicapIndex = findHandicapIndexAtDate.find(handicapIndex);
            ec.competitionData().setCmpDataHandicap(handicapIndex.getHandicapWHS().doubleValue());
            liste.add(ec);
        }

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
        LOG.debug("from main, CompetitionInscriptionsList = ");
    } // end main
    */

} // end class

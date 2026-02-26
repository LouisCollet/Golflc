package lists;

import entite.CompetitionDescription;
import entite.composite.ECompetition;
import entite.HandicapIndex;
import static exceptions.LCException.handleGenericException;
import static exceptions.LCException.handleSQLException;
import static interfaces.Log.LOG;
import jakarta.annotation.Resource;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
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

@Named
@ApplicationScoped
public class CompetitionInscriptionsList implements Serializable {

    private static final long serialVersionUID = 1L;

    @Resource(lookup = "java:jboss/datasources/golflc")
    private DataSource dataSource;

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

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setInt(1, cd.getCompetitionId());
            utils.LCUtil.logps(ps);

            try (ResultSet rs = ps.executeQuery()) {
                liste = new ArrayList<>();
                while (rs.next()) {
                    var description = entite.CompetitionDescription.map(rs);
                    var data = entite.CompetitionData.map(rs);
                    ECompetition ec = new ECompetition(description, data);
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
            }

        } catch (SQLException e) {
            handleSQLException(e, methodName);
            return Collections.emptyList();
        } catch (Exception e) {
            handleGenericException(e, methodName);
            return Collections.emptyList();
        }
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

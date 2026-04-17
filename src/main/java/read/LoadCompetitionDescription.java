package read;

import entite.CompetitionDescription;
import static exceptions.LCException.handleGenericException;
import static exceptions.LCException.handleSQLException;
import static interfaces.Log.LOG;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.io.Serializable;
import java.sql.SQLException;

@ApplicationScoped
public class LoadCompetitionDescription implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject private dao.GenericDAO dao;

    public LoadCompetitionDescription() { }

    public CompetitionDescription load(final CompetitionDescription competition) throws SQLException {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        LOG.debug("with competition = {}", competition);

        final String query = """
                SELECT *
                FROM competition_description
                WHERE CompetitionId = ?
                """;

        return dao.querySingle(query, rs -> {
            try {
                return CompetitionDescription.map(rs);
            } catch (Exception e) {
                throw new SQLException(e);
            }
        }, competition.getCompetitionId());
    } // end method

    /*
    void main() throws SQLException {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        // nécessite contexte CDI — DataSource injecté par WildFly
        // CompetitionDescription competition = new CompetitionDescription();
        // competition.setCompetitionId(24);
        // competition = new LoadCompetitionDescription().load(competition);
        // LOG.debug(" loaded competition description = {}", competition);
    } // end main
    */

} // end class

package read;

import entite.CompetitionData;
import static exceptions.LCException.handleGenericException;
import static exceptions.LCException.handleSQLException;
import static interfaces.Log.LOG;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.io.Serializable;
import java.sql.SQLException;

@ApplicationScoped
public class LoadCompetitionData implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject private dao.GenericDAO dao;

    public LoadCompetitionData() { }

    public CompetitionData load(final CompetitionData competition) throws SQLException {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName);
        LOG.debug(methodName + " - with competition = " + competition);

        final String query = """
                SELECT *
                FROM competition_data
                WHERE CmpDataId = ?
                """;

        return dao.querySingle(query, rs -> CompetitionData.map(rs), competition.getCmpDataId());
    } // end method

    /*
    void main() throws SQLException {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName);
        // nécessite contexte CDI — DataSource injecté par WildFly
        // CompetitionData competition = new CompetitionData();
        // competition.setCmpDataId(25);
        // competition = new LoadCompetitionData().load(competition);
        // LOG.debug(" loaded Competition Data = " + competition);
    } // end main
    */

} // end class

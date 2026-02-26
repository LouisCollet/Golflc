package read;

import entite.CompetitionDescription;
import static exceptions.LCException.handleGenericException;
import static exceptions.LCException.handleSQLException;
import static interfaces.Log.LOG;
import jakarta.annotation.Resource;
import jakarta.enterprise.context.ApplicationScoped;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.sql.DataSource;

@ApplicationScoped
public class LoadCompetitionDescription implements Serializable {

    private static final long serialVersionUID = 1L;

    @Resource(lookup = "java:jboss/datasources/golflc")
    private DataSource dataSource;

    public LoadCompetitionDescription() { }

    public CompetitionDescription load(final CompetitionDescription competition) throws SQLException {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName);
        LOG.debug(methodName + " - with competition = " + competition);

        final String query = """
                SELECT *
                FROM competition_description
                WHERE CompetitionId = ?
                """;

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setInt(1, competition.getCompetitionId());
            utils.LCUtil.logps(ps);

            try (ResultSet rs = ps.executeQuery()) {
                CompetitionDescription cd = new CompetitionDescription();
                int i = 0;
                while (rs.next()) {
                    i++;
                    cd = CompetitionDescription.map(rs);
                }
                if (i == 0) {
                    LOG.warn(methodName + " - nothing found for CompetitionId = " + competition.getCompetitionId());
                    return null;
                }
                return cd;
            }

        } catch (SQLException e) {
            handleSQLException(e, methodName);
            return null;
        } catch (Exception e) {
            handleGenericException(e, methodName);
            return null;
        }
    } // end method

    /*
    void main() throws SQLException {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName);
        // nécessite contexte CDI — DataSource injecté par WildFly
        // CompetitionDescription competition = new CompetitionDescription();
        // competition.setCompetitionId(24);
        // competition = new LoadCompetitionDescription().load(competition);
        // LOG.debug(" loaded competition description = " + competition);
    } // end main
    */

} // end class

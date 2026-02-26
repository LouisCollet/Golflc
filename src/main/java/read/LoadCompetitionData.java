package read;

import entite.CompetitionData;
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
public class LoadCompetitionData implements Serializable {

    private static final long serialVersionUID = 1L;

    @Resource(lookup = "java:jboss/datasources/golflc")
    private DataSource dataSource;

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

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setInt(1, competition.getCmpDataId());
            utils.LCUtil.logps(ps);

            try (ResultSet rs = ps.executeQuery()) {
                CompetitionData cd = new CompetitionData();
                while (rs.next()) {
                    cd = CompetitionData.map(rs);
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
        // CompetitionData competition = new CompetitionData();
        // competition.setCmpDataId(25);
        // competition = new LoadCompetitionData().load(competition);
        // LOG.debug(" loaded Competition Data = " + competition);
    } // end main
    */

} // end class

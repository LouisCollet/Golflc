package delete;

import entite.composite.ECompetition;
import static exceptions.LCException.handleGenericException;
import static exceptions.LCException.handleSQLException;
import static interfaces.Log.LOG;
import jakarta.annotation.Resource;
import jakarta.enterprise.context.ApplicationScoped;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import javax.sql.DataSource;
import utils.LCUtil;

@ApplicationScoped
public class DeleteInscriptionCompetition implements Serializable {

    private static final long serialVersionUID = 1L;

    @Resource(lookup = "java:jboss/datasources/golflc")
    private DataSource dataSource;

    public DeleteInscriptionCompetition() { }

    public boolean delete(final ECompetition competition) throws SQLException {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName);
        LOG.debug("for competition = " + competition);

        final String query = """
            DELETE FROM competition_data
            WHERE CmpDataId = ?
              AND CmpDataPlayerId = ?
            """;

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setInt(1, competition.competitionData().getCmpDataId());
            ps.setInt(2, competition.competitionData().getCmpDataPlayerId());
            LCUtil.logps(ps);
            int rowDeleted = ps.executeUpdate();
            String msg = "There is " + rowDeleted + " Competition Data deleted !";
            LOG.debug(msg);
            LCUtil.showMessageInfo(msg);
            return true;

        } catch (SQLException e) {
            handleSQLException(e, methodName);
            return false;
        } catch (Exception e) {
            handleGenericException(e, methodName);
            return false;
        }
    } // end method

    // ===========================================================================================
    // BRIDGE — @Deprecated — pour les appelants legacy (new DeleteInscriptionCompetition().delete(ec, conn))
    // À supprimer quand tous les appelants seront migrés en CDI
    // ===========================================================================================
    /** @deprecated Utiliser {@link #delete(ECompetition)} via injection CDI */
    /*
    void main() throws SQLException {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName);
        // ECompetition ec = ...;
        // boolean b = delete(ec);
        // LOG.debug("deleted = " + b);
    } // end main
    */

} // end class

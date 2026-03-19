package delete;

import entite.composite.ECompetition;
import static exceptions.LCException.handleGenericException;
import static exceptions.LCException.handleSQLException;
import static interfaces.Log.LOG;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.io.Serializable;
import java.sql.SQLException;
import utils.LCUtil;

@ApplicationScoped
public class DeleteInscriptionCompetition implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject private dao.GenericDAO dao;

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

        int rowDeleted = dao.execute(query,
                competition.competitionData().getCmpDataId(),
                competition.competitionData().getCmpDataPlayerId());
        String msg = "There is " + rowDeleted + " Competition Data deleted !";
        LOG.debug(msg);
        LCUtil.showMessageInfo(msg);
        return true;
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

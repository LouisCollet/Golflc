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
        LOG.debug("entering {}", methodName);
        LOG.debug("for competition = {}", competition);

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

} // end class

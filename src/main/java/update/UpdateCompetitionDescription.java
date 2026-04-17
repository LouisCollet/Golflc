package update;

import com.fasterxml.jackson.databind.ObjectMapper;
import entite.CompetitionDescription;
import static exceptions.LCException.handleGenericException;
import static exceptions.LCException.handleSQLException;
import static interfaces.Log.LOG;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import utils.LCUtil;

@ApplicationScoped
public class UpdateCompetitionDescription implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject
    private dao.GenericDAO dao;

    public UpdateCompetitionDescription() { }

    public boolean update(final CompetitionDescription cd) throws SQLException {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        LOG.debug("with CompetitionDescription = {}", cd);

        try (Connection conn = dao.getConnection()) {

            String co = utils.DBMeta.listMetaColumnsUpdate(conn, "competition_description");
            LOG.debug("columns = {}", co);

            final String query = """
                UPDATE competition_description
                SET %s
                WHERE CompetitionId = ?
                """.formatted(co);

            try (PreparedStatement ps = conn.prepareStatement(query)) {
                CompetitionDescription.psCompetitionDescriptionModify(ps, cd);
                utils.LCUtil.logps(ps);

                int row = ps.executeUpdate();
                LOG.debug("rows modified = {}", row);
                if (row != 0) {
                    String msg = LCUtil.prepareMessageBean("competition.description.modify")
                            + " <br/>ID = " + cd.getCompetitionId()
                            + " <br/>Name = " + cd.getCompetitionName();
                    LOG.debug(msg);
                    LCUtil.showMessageInfo(msg);
                    return true;
                } else {
                    String msg = "-- NOT NOT successful " + methodName;
                    LOG.debug(msg);
                    LCUtil.showMessageInfo(msg);
                    return false;
                }
            }

        } catch (SQLException e) {
            handleSQLException(e, methodName);
            return false;
        } catch (Exception e) {
            handleGenericException(e, methodName);
            return false;
        }
    } // end method

    /*
    void main() throws SQLException {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        CompetitionDescription cd = new CompetitionDescription();
        cd.setCompetitionId(25);
        // boolean b = update(cd);
        LOG.debug("from main, UpdateCompetitionDescription = ");
    } // end main
    */

} // end class

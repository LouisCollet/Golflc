package create;

import entite.CompetitionData;
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
public class CreateCompetitionData implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject private dao.GenericDAO dao;

    public CreateCompetitionData() { }

    public boolean create(final CompetitionData data) throws SQLException {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        LOG.debug("with competitionData = {}", data);

        try (Connection conn = dao.getConnection()) {

            final String query = LCUtil.generateInsertQuery(conn, "competition_data");

            try (PreparedStatement ps = conn.prepareStatement(query)) {
                sql.preparedstatement.psCreateUpdateCompetitionData.psMapCreate(ps, data);

                int row = ps.executeUpdate();
                if (row != 0) {
                    data.setCmpDataId(LCUtil.generatedKey(conn));
                    LOG.debug("Successful update CompetitionData");
                    String msg = LCUtil.prepareMessageBean("competition.data.create") + data + "<br>" + data;
                    LOG.debug(msg);
                    LCUtil.showMessageInfo(msg);
                    return true;
                } else {
                    String msg = methodName + " - ERROR update competitionData : " + data;
                    LOG.error(msg);
                    LCUtil.showMessageFatal(msg);
                    return false;
                }
            }

        } catch (SQLException sqle) {
            String msg;
            if (sqle.getSQLState().equals("23000") && sqle.getErrorCode() == 1062) {
                msg = LCUtil.prepareMessageBean("create.competitiondata.duplicate")
                        + " competition = " + data.getCmpDataCompetitionId();
            } else {
                msg = "SQLException in " + methodName + " " + sqle.getMessage()
                        + " ,SQLState = " + sqle.getSQLState()
                        + " ,ErrorCode = " + sqle.getErrorCode();
            }
            LOG.error(msg);
            LCUtil.showMessageFatal(msg);
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
        // var b = create(data);
        // LOG.debug("from main, b = {}", b);
        LOG.debug("from main, CreateCompetitionData = ");
    } // end main
    */

} // end class

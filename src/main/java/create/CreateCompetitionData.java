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
import java.sql.Time;
import java.sql.Timestamp;
import java.time.Instant;
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
            int index = 0;

            try (PreparedStatement ps = conn.prepareStatement(query)) {
                ps.setNull(++index, java.sql.Types.INTEGER); // CmpDataId
                ps.setInt(2, data.getCmpDataCompetitionId());
                ps.setInt(3, data.getCmpDataPlayerId());
                ps.setShort(4, (short) 0); // playingHandicap
                ps.setDouble(5, 0); // handicap
                ps.setTime(6, Time.valueOf("00:00:00")); // flight start
                ps.setShort(7, (short) 0); // flight number
                ps.setShort(8, (short) 0); // scorepoints
                ps.setString(9, data.getCmpDataLastHoles());
                ps.setString(10, data.getCmpDataPlayerFirstLastName());
                ps.setString(11, data.getCmpDataAskedStartTime());
                ps.setString(12, data.getCmpDataPlayerGender());
                ps.setInt(13, 0); // CmpDataRoundId
                ps.setString(14, ""); // CmpDataTeeStart
                ps.setDouble(15, 0); // score differential
                ps.setTimestamp(16, Timestamp.from(Instant.now()));
                utils.LCUtil.logps(ps);

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

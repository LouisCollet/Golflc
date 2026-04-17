package update;

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
public class UpdateCompetitionData implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject
    private dao.GenericDAO dao;

    public UpdateCompetitionData() { }

    public boolean update(final CompetitionData cda) throws SQLException {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        LOG.debug("with CompetitionData = {}", cda);

        try (Connection conn = dao.getConnection()) {

            String co = utils.DBMeta.listMetaColumnsUpdate(conn, "competition_data");
            final String query = "UPDATE competition_data SET " + co + " WHERE CmpDataId = ?";

            try (PreparedStatement ps = conn.prepareStatement(query)) {
                ps.setInt(1, cda.getCmpDataPlayerId());
                ps.setShort(2, cda.getCmpDataPlayingHandicap());
                ps.setDouble(3, cda.getCmpDataHandicap());
                ps.setTime(4, Time.valueOf(cda.getCmpDataFlightStart()));
                ps.setShort(5, cda.getCmpDataFlightNumber());
                ps.setShort(6, cda.getCmpDataScorePoints());
                ps.setString(7, cda.getCmpDataLastHoles());
                ps.setString(8, cda.getCmpDataPlayerFirstLastName());
                ps.setString(9, cda.getCmpDataAskedStartTime());
                ps.setString(10, cda.getCmpDataPlayerGender());
                ps.setInt(11, cda.getCmpDataRoundId());
                ps.setString(12, cda.getCmpDataTeeStart());
                ps.setDouble(13, cda.getCmpDataScoreDifferential());
                ps.setTimestamp(14, Timestamp.from(Instant.now()));
                ps.setInt(15, cda.getCmpDataId());
                utils.LCUtil.logps(ps);

                int row = ps.executeUpdate();
                LOG.debug("rows modified = {}", row);
                if (row != 0) {
                    String msg = " <br/>ID = " + cda.getCmpDataId()
                            + " <br/>Start Time = " + cda.getCmpDataAskedStartTime();
                    LOG.debug(msg);
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
        CompetitionData cda = new CompetitionData();
        cda.setCmpDataId(25);
        // boolean b = update(cda);
        LOG.debug("from main, UpdateCompetitionData = ");
    } // end main
    */

} // end class

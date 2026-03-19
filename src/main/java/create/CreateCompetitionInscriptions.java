package create;

import entite.CompetitionDescription;
import entite.CompetitionData;
import entite.Course;
import entite.composite.ECompetition;
import entite.Round;
import entite.UnavailablePeriod;
import static exceptions.LCException.handleGenericException;
import static exceptions.LCException.handleSQLException;
import static interfaces.GolfInterface.ZDF_TIME_HHmm;
import static interfaces.Log.LOG;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import utils.LCUtil;

@ApplicationScoped
public class CreateCompetitionInscriptions implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject private dao.GenericDAO dao;

    @Inject private lists.CompetitionRoundsList    competitionRoundsList;
    @Inject private update.UpdateCompetitionData   updateCompetitionData;

    public CreateCompetitionInscriptions() { }

    public boolean create(final CompetitionDescription cd) throws SQLException {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName);
        LOG.debug(methodName + " - with description = " + cd);
        LOG.debug(methodName + " - competition Status = " + cd.getCompetitionStatus());
        try {
            if (!cd.getCompetitionStatus().equals("2")) {
                LOG.debug(methodName + " - wrong Status, must be = 2 but is = " + cd.getCompetitionStatus());
                return false;
            }

            List<ECompetition> li = competitionRoundsList.list(cd);
            for (int i = 0; i < li.size(); i++) {
                LOG.debug("flightnumber = " + li.get(i).competitionData().getCmpDataFlightNumber());
                LOG.debug(" - StartTime = " + li.get(i).competitionData().getCmpDataFlightStart());
                LOG.debug(" - roundId = " + li.get(i).competitionData().getCmpDataRoundId());
                LOG.debug(" - TeeStart = " + li.get(i).competitionData().getCmpDataTeeStart());
            }

            for (int i = 0; i < li.size(); i++) {
                var competition = li.get(i);
                var data = li.get(i).competitionData();
                LOG.debug("round ID = " + data.getCmpDataRoundId());
                LOG.debug("start time = " + data.getCmpDataFlightStart());
                if (!this.createOneInscription(competition)) {
                    String msg = methodName + " - createOneInscription NOT created!";
                    LOG.error(msg);
                    LCUtil.showMessageFatal(msg);
                    return false;
                }
                if (!updateCompetitionData.update(data)) {
                    String msg = methodName + " - NOT modify CompetitionData !!";
                    LOG.error(msg);
                    LCUtil.showMessageFatal(msg);
                    return false;
                }
            } // end for
            return true;

        } catch (SQLException e) {
            handleSQLException(e, methodName);
            return false;
        } catch (Exception e) {
            handleGenericException(e, methodName);
            return false;
        }
    } // end method

    public boolean createOneInscription(final ECompetition ec) throws SQLException {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName);
        CompetitionDescription cde = null;
        CompetitionData cda = null;
        try {
            cda = ec.competitionData();
            cde = ec.competitionDescription();

            try (Connection conn = dao.getConnection()) {

                final String query = LCUtil.generateInsertQuery(conn, "player_has_round");

                try (PreparedStatement ps = conn.prepareStatement(query)) {
                    LOG.debug(methodName + " - starting createOneInscription");
                    ps.setNull(1, java.sql.Types.INTEGER);
                    ps.setInt(2, cda.getCmpDataRoundId());
                    ps.setInt(3, cda.getCmpDataPlayerId());
                    ps.setInt(4, 0); // Final Results initial value
                    ps.setInt(5, 0); // NotUsed1 initial value
                    ps.setInt(6, 0); // NotUsed2 initial value
                    ps.setString(7, cda.getCmpDataTeeStart());
                    String TeeStart = cda.getCmpDataTeeStart();
                    int tee = Integer.valueOf(TeeStart
                            .substring(TeeStart.lastIndexOf("/") + 2, TeeStart.length()));
                    LOG.debug(methodName + " - tee extracted from inscriptionTeeStart = " + tee);
                    ps.setInt(8, tee);
                    ps.setInt(9, cda.getCmpDataPlayerId());
                    ps.setTimestamp(10, Timestamp.from(Instant.now()));
                    utils.LCUtil.logps(ps);

                    int x = ps.executeUpdate();
                    if (x != 0) {
                        String msg = "" + cde.getCompetitionId()
                                + " <br/>genre = " + cde.getCompetitionGame()
                                + " <br/>competition = " + cde.getCompetitionName()
                                + " <br/>date = " + cde.getCompetitionDate().format(ZDF_TIME_HHmm);
                        LOG.debug(msg);
                        LCUtil.showMessageInfo(msg);
                        return true;
                    } else {
                        String msg = "<br/>NOT NOT Successful " + methodName + cde.getCompetitionId()
                                + " <br/>genre = " + cde.getCompetitionGame()
                                + " <br/>competition = " + cde.getCompetitionName()
                                + " <br/>date = " + cde.getCompetitionDate().format(ZDF_TIME_HHmm);
                        LOG.error(msg);
                        LCUtil.showMessageFatal(msg);
                        return false;
                    }
                }
            }

        } catch (SQLException sqle) {
            String msg = "exception in " + methodName + " " + sqle.getMessage()
                    + " ,SQLState = " + sqle.getSQLState() + " ,ErrorCode = " + sqle.getErrorCode();
            LOG.error(msg);
            LCUtil.showMessageFatal(msg);
            return false;
        } catch (Exception e) {
            String msg = "Exception in " + methodName + " " + e.getMessage()
                    + " for competition = " + (cde != null ? cde.getCompetitionId() : "null");
            LOG.error(msg);
            LCUtil.showMessageFatal(msg);
            return false;
        }
    } // end method

    public boolean validate(final Round round, final Course course, final UnavailablePeriod unavailable) throws SQLException {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName);
        try {
            LocalDateTime cb = course.getCourseBeginDate();
            LOG.debug(methodName + " - LocalDateTime courseBegin = " + cb);
            if (round.getRoundDate().isBefore(cb)) {
                String msgerr = LCUtil.prepareMessageBean("round.notopened");
                LOG.error(msgerr);
                LCUtil.showMessageFatal(msgerr);
                return false;
            }
            LocalDateTime ce = course.getCourseEndDate();
            LOG.debug(methodName + " - LocalDateTime courseEnd = " + ce);
            if (round.getRoundDate().isAfter(ce)) {
                String msgerr = LCUtil.prepareMessageBean("round.closed");
                LOG.error(msgerr);
                LCUtil.showMessageFatal(msgerr);
                return false;
            }
            return true;

        } catch (Exception e) {
            handleGenericException(e, methodName);
            return false;
        }
    } // end method

    /*
    void main() throws SQLException {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName);
        // CompetitionDescription cd = new CompetitionDescription();
        // cd.setCompetitionId(27);
        // boolean b = create(cd);
        // LOG.debug("from main, b = " + b);
        LOG.debug("from main, CreateCompetitionInscriptions = ");
    } // end main
    */

} // end class

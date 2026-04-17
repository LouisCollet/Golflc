package create;

import entite.CompetitionDescription;
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
public class CreateCompetitionRounds implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject private dao.GenericDAO dao;

    @Inject private lists.CompetitionRoundsList                 competitionRoundsList;
    @Inject private calc.CalcCompetitionInscriptionTeeStart     calcCompetitionInscriptionTeeStart;
    @Inject private update.UpdateCompetitionData                updateCompetitionData;

    private int GENERATED_KEY = 0;

    public CreateCompetitionRounds() { }

    public boolean create(final CompetitionDescription cd) throws SQLException {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        LOG.debug("competition Status = {}", cd.getCompetitionStatus());
        try {
            if (!cd.getCompetitionStatus().equals("1")) {
                LOG.debug("wrong Status, must be = 1 but is = {}", cd.getCompetitionStatus());
                return false;
            }

            List<ECompetition> li = competitionRoundsList.list(cd);
            LOG.debug("there are rounds = {}", li.size());
            for (int i = 0; i < li.size(); i++) {
                LOG.debug("flightnumber = {} StartTime = {}", li.get(i).competitionData().getCmpDataFlightNumber(), li.get(i).competitionData().getCmpDataFlightStart());
            }

            int save = 0;
            for (int i = 0; i < li.size(); i++) {
                var competition = li.get(i);
                var description = competition.competitionDescription();
                var data = li.get(i).competitionData();
                LOG.debug("flightnumber = {}", data.getCmpDataFlightNumber());
                LOG.debug("start time = {}", data.getCmpDataFlightStart());
                LOG.debug("save = {}", save);
                if (data.getCmpDataFlightNumber() != save) {
                    LOG.debug("we do both!");
                    competition.withCompetitionDescription(description);
                    if (!this.createOneRound(competition)) {
                        String msg = methodName + " - createOneRound NOT created!";
                        LOG.error(msg);
                        LCUtil.showMessageFatal(msg);
                        return false;
                    }
                } else {
                    LOG.debug("we do only modify Data!");
                }
                LOG.debug("GENERATED_KEY used = {}", GENERATED_KEY);
                data.setCmpDataRoundId(GENERATED_KEY);
                LOG.debug("generated-key inserted = {}", data.getCmpDataRoundId());
                String teeStart = calcCompetitionInscriptionTeeStart.calc(competition);
                data.setCmpDataTeeStart(teeStart);
                LOG.debug("TeeStart inserted = {}", data.getCmpDataTeeStart());
                if (!updateCompetitionData.update(data)) {
                    String msg = methodName + " - NOT modify CompetitionData !! " + data.getCmpDataRoundId();
                    LOG.error(msg);
                    LCUtil.showMessageFatal(msg);
                    return false;
                } else {
                    String msg = methodName + " - OK modify CompetitionData for RoundId = "
                            + data.getCmpDataRoundId() + " for player = " + data.getCmpDataPlayerId();
                    LOG.info(msg);
                    LCUtil.showMessageInfo(msg);
                }
                save = data.getCmpDataFlightNumber();
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

    public boolean createOneRound(final ECompetition ec) throws SQLException {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        CompetitionDescription de = null;
        try {
            de = ec.competitionDescription();

            try (Connection conn = dao.getConnection()) {

                final String query = LCUtil.generateInsertQuery(conn, "round");

                try (PreparedStatement ps = conn.prepareStatement(query)) {
                    LOG.debug("starting createOneRound");
                    LocalDateTime ldt = de.getCompetitionDate().toLocalDate()
                            .atTime(ec.competitionData().getCmpDataFlightStart());
                    ps.setNull(1, java.sql.Types.INTEGER);
                    ps.setTimestamp(2, Timestamp.valueOf(ldt));
                    ps.setString(3, de.getCompetitionGame());
                    ps.setInt(4, 0);
                    ps.setString(5, de.getCompetitionName());
                    ps.setString(6, de.getCompetitionQualifying());
                    ps.setInt(7, 18);
                    ps.setInt(8, de.getCompetitionStartHole());
                    String e = "0";
                    byte[] b = e.getBytes();
                    ps.setBytes(9, b);
                    ps.setString(10, "no MP score");
                    ps.setInt(11, 0);
                    ps.setString(12, de.getCompetitionName());
                    ps.setInt(13, de.getCompetitionCourseId());
                    ps.setTimestamp(14, Timestamp.from(Instant.now()));
                    utils.LCUtil.logps(ps);

                    int x = ps.executeUpdate();
                    if (x != 0) {
                        this.GENERATED_KEY = LCUtil.generatedKey(conn);
                        String msg = "One Round Created = " + de.getCompetitionId()
                                + " / round = " + GENERATED_KEY
                                + " <br/>genre = " + de.getCompetitionGame()
                                + " <br/>competition = " + de.getCompetitionName()
                                + " <br/>date = " + de.getCompetitionDate().format(ZDF_TIME_HHmm);
                        LOG.debug(msg);
                        LCUtil.showMessageInfo(msg);
                        return true;
                    } else {
                        String msg = "<br/>NOT NOT Successful " + methodName + de.getCompetitionId()
                                + " <br/>genre = " + de.getCompetitionGame()
                                + " <br/>competition = " + de.getCompetitionName()
                                + " <br/>date = " + de.getCompetitionDate().format(ZDF_TIME_HHmm);
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
                    + " for competition = " + (de != null ? de.getCompetitionId() : "null");
            LOG.error(msg);
            LCUtil.showMessageFatal(msg);
            return false;
        }
    } // end method

    public boolean validate(final Round round, final Course course, final UnavailablePeriod unavailable) throws SQLException {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        try {
            LocalDateTime cb = course.getCourseBeginDate();
            LOG.debug("LocalDateTime courseBegin = {}", cb);
            if (round.getRoundDate().isBefore(cb)) {
                String msgerr = LCUtil.prepareMessageBean("round.notopened");
                LOG.error(msgerr);
                LCUtil.showMessageFatal(msgerr);
                return false;
            }
            LocalDateTime ce = course.getCourseEndDate();
            LOG.debug("LocalDateTime courseEnd = {}", ce);
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
        LOG.debug("entering {}", methodName);
        // CompetitionDescription cde = new CompetitionDescription();
        // cde.setCompetitionId(999);
        // boolean b = create(cde);
        // LOG.debug("from main, b = {}", b);
        LOG.debug("from main, CreateCompetitionRounds = ");
    } // end main
    */

} // end class

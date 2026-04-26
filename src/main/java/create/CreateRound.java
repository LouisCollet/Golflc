package create;

import entite.Club;
import entite.Course;
import entite.Round;
import entite.UnavailablePeriod;
import static exceptions.LCException.handleGenericException;
import static exceptions.LCException.handleSQLException;
import static interfaces.GolfInterface.ZDF_TIME;
import static interfaces.GolfInterface.ZDF_TIME_HHmm;
import static interfaces.Log.LOG;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import static utils.LCUtil.showMessageFatal;
import static utils.LCUtil.showMessageInfo;

@ApplicationScoped
public class CreateRound implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject private dao.GenericDAO dao;

    public CreateRound() { }

    public boolean create(final Round round, final Course course,
                          final Club club, final UnavailablePeriod unavailable) throws SQLException {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        LOG.debug("round = {}", round);
        LOG.debug("course = {}", course);
        LOG.debug("club   = {}", club);

        if (!validate(round, course, unavailable)) {
            LOG.debug("validation failed, aborting");
            return false;
        }

        try (Connection conn      = dao.getConnection();
             PreparedStatement ps = conn.prepareStatement(utils.LCUtil.generateInsertQuery(conn, "round"))) {

            // Conversion date locale → UTC pour stockage en base
            ZonedDateTime zdt = round.getRoundDate()
                    .atZone(ZoneId.of(club.getAddress().getZoneId()))
                    .withZoneSameInstant(ZoneId.of("UTC"));
            LocalDateTime ldt = zdt.toLocalDateTime();
            LOG.debug("ZoneId = {}, UTC = {}", club.getAddress().getZoneId(), ldt);

            sql.preparedstatement.psCreateRound.psMapCreate(ps, round, course, ldt);
            utils.LCUtil.logps(ps);
            int x = ps.executeUpdate();

            if (x != 0) {
                round.setIdround(utils.LCUtil.generatedKey(conn));
                LOG.debug("round created id={} game={} name={} holes={} start={} date={} utc={}",
                        round.getIdround(), round.getRoundGame(), round.getRoundName(),
                        round.getRoundHoles(), round.getRoundStart(),
                        round.getRoundDate().format(ZDF_TIME_HHmm), zdt.format(ZDF_TIME_HHmm));
                showMessageInfo(utils.LCUtil.prepareMessageBean("round.created") + " " + round.getIdround()
                        + " — " + round.getRoundDate().format(ZDF_TIME_HHmm));
                return true;
            } else {
                LOG.error("insert round returned 0 rows — id={} game={} date={}",
                        round.getIdround(), round.getRoundGame(),
                        round.getRoundDate().format(ZDF_TIME));
                showMessageFatal(utils.LCUtil.prepareMessageBean("round.error"));
                return false;
            }

        } catch (SQLException sqle) {
            handleSQLException(sqle, methodName);
            return false;
        } catch (Exception e) {
            handleGenericException(e, methodName);
            return false;
        }
    } // end method

    public boolean validate(final Round round, final Course course,
                            final UnavailablePeriod unavailable) {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        try {
            if (round.getRoundDate() == null) {
                LOG.error("roundDate is null");
                showMessageFatal(utils.LCUtil.prepareMessageBean("round.date.required"));
                return false;
            }
            LocalDateTime cb = course.getCourseBeginDate();
            LOG.debug("courseBegin = {}", cb);
            if (round.getRoundDate().isBefore(cb)) {
                LOG.error("roundDate {} before courseBegin {}", round.getRoundDate(), cb);
                showMessageFatal(utils.LCUtil.prepareMessageBean("round.notopened"));
                return false;
            }
            LocalDateTime ce = course.getCourseEndDate();
            LOG.debug("courseEnd = {}", ce);
            if (round.getRoundDate().isAfter(ce)) {
                LOG.error("roundDate {} after courseEnd {}", round.getRoundDate(), ce);
                showMessageFatal(utils.LCUtil.prepareMessageBean("round.closed"));
                return false;
            }
            return true;
        } catch (Exception e) {
            handleGenericException(e, methodName);
            return false;
        }
    } // end method

    /*
    void main() throws Exception {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
    } // end main
    */

} // end class

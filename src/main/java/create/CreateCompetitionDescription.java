package create;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import entite.CompetitionDescription;
import entite.ValidationsLC;
import entite.ValidationsLC.ValidationStatus;
import static exceptions.LCException.handleGenericException;
import static exceptions.LCException.handleSQLException;
import static interfaces.Log.LOG;
import static interfaces.Log.NEW_LINE;
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
import static utils.LCUtil.LocalDateTimeToDate;

@ApplicationScoped
public class CreateCompetitionDescription implements Serializable {

    private static final long serialVersionUID = 1L;

    private static final ObjectMapper OBJECT_MAPPER;
    static {
        OBJECT_MAPPER = new ObjectMapper();
        OBJECT_MAPPER.registerModule(new JavaTimeModule());
        OBJECT_MAPPER.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
    }

    @Inject private dao.GenericDAO dao;

    ValidationsLC vlc = new ValidationsLC();

    public CreateCompetitionDescription() { }

    public boolean create(final CompetitionDescription competition) throws SQLException {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        LOG.debug("for competition = {}", competition);

        try {
            vlc = this.validate(competition);
            LOG.debug("vlc = {}", vlc);
            if (vlc.getStatus0().equals(ValidationStatus.REJECTED.toString())) {
                LOG.error(vlc.getStatus1());
                LCUtil.showMessageFatal(vlc.getStatus1());
                return false;
            }

            try (Connection conn = dao.getConnection()) {

                final String query = LCUtil.generateInsertQuery(conn, "competition_description");
                int index = 0;

                try (PreparedStatement ps = conn.prepareStatement(query)) {
                    ps.setNull(++index, java.sql.Types.INTEGER); // CompetitionId
                    ps.setTimestamp(2, Timestamp.valueOf(competition.getCompetitionDate()));
                    ps.setString(3, competition.getCompetitionName());
                    ps.setTimestamp(4, Timestamp.valueOf(competition.getStartInscriptionDate()));
                    ps.setTimestamp(5, Timestamp.valueOf(competition.getEndInscriptionDate()));
                    ps.setInt(6, competition.getCompetitionClubId());
                    ps.setString(7, competition.getCompetitionCourseIdName());
                    ps.setString(8, competition.getCompetitionGender());
                    ps.setString(9, competition.getCompetitionGame());
                    ps.setShort(10, competition.getCompetitionStartHole());
                    ps.setShort(11, competition.getFlightNumberPlayers());
                    ps.setString(12, competition.getTimeSlots());
                    String json = OBJECT_MAPPER.writeValueAsString(competition);
                    LOG.debug("seriesHandicap converted in json = {}", NEW_LINE + json);
                    ps.setString(13, json);
                    ps.setString(14, competition.getCompetitionQualifying());
                    ps.setTime(15, Time.valueOf(competition.getPriceGivingTime()));
                    ps.setTimestamp(16, Timestamp.valueOf(competition.getStartingListDate()));
                    ps.setTimestamp(17, Timestamp.valueOf(competition.getClassmentDate()));
                    ps.setShort(18, (short) 72); // CompetitionPar
                    ps.setString(19, "0"); // CompetitionStatus
                    ps.setShort(20, competition.getCompetitionAgeLadies());
                    ps.setShort(21, competition.getCompetitionAgeMens());
                    ps.setShort(22, competition.getCompetitionMaximumPlayers());
                    ps.setTimestamp(23, Timestamp.from(Instant.now()));
                    utils.LCUtil.logps(ps);

                    int row = ps.executeUpdate();
                    if (row != 0) {
                        competition.setCompetitionId(LCUtil.generatedKey(conn));
                        String msg = LCUtil.prepareMessageBean("competition.create") + competition;
                        LOG.debug(msg);
                        LCUtil.showMessageInfo(msg);
                        return true;
                    } else {
                        String msg = methodName + " - ERROR update competitionDescription : " + competition;
                        LOG.error(msg);
                        LCUtil.showMessageFatal(msg);
                        return false;
                    }
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

    public ValidationsLC validate(final CompetitionDescription competition) throws SQLException {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        try {
            vlc.setStatus0(ValidationsLC.ValidationStatus.APPROVED.toString());
            if (competition.getEndInscriptionDate().isBefore(competition.getStartInscriptionDate())) {
                vlc.setStatus0(ValidationsLC.ValidationStatus.REJECTED.toString());
                Object[] data = new Object[2];
                data[0] = competition.getEndInscriptionDate();
                data[1] = LocalDateTimeToDate(competition.getStartInscriptionDate());
                String msgerr = LCUtil.prepareMessageBean1("competition.endbefore.start", data);
                vlc.setStatus1(msgerr);
                return vlc;
            }
            if (competition.getCompetitionDate().isBefore(competition.getStartInscriptionDate())) {
                vlc.setStatus0(ValidationsLC.ValidationStatus.REJECTED.toString());
                Object[] data = new Object[2];
                data[0] = LocalDateTimeToDate(competition.getCompetitionDate());
                data[1] = LocalDateTimeToDate(competition.getStartInscriptionDate());
                String msgerr = LCUtil.prepareMessageBean1("competition.datebefore.start", data);
                vlc.setStatus1(msgerr);
                return vlc;
            }
            return vlc; // is approved

        } catch (Exception e) {
            handleGenericException(e, methodName);
            return null;
        }
    } // end method

    /*
    void main() throws SQLException {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        // var b = create(competition);
        // LOG.debug("from main, b = {}", b);
        LOG.debug("from main, CreateCompetitionDescription = ");
    } // end main
    */

} // end class

package sql.preparedstatement;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import entite.CompetitionDescription;
import static exceptions.LCException.handleGenericException;
import static interfaces.Log.LOG;
import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.Time;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.Instant;

public class psCreateCompetitionDescription implements Serializable {

    private static final long serialVersionUID = 1L;

    private static final ObjectMapper OBJECT_MAPPER;
    static {
        OBJECT_MAPPER = new ObjectMapper();
        OBJECT_MAPPER.registerModule(new JavaTimeModule());
        OBJECT_MAPPER.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
    }

    public static PreparedStatement psMapCreate(
            PreparedStatement ps,
            final CompetitionDescription competition) throws Exception {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        try {
            String json = OBJECT_MAPPER.writeValueAsString(competition);
            ps.setNull     (1,  Types.INTEGER);                                    // CompetitionId — auto-increment
            ps.setTimestamp(2,  Timestamp.valueOf(competition.getCompetitionDate()));
            ps.setString   (3,  competition.getCompetitionName());
            ps.setTimestamp(4,  Timestamp.valueOf(competition.getStartInscriptionDate()));
            ps.setTimestamp(5,  Timestamp.valueOf(competition.getEndInscriptionDate()));
            ps.setInt      (6,  competition.getCompetitionClubId());
            ps.setString   (7,  competition.getCompetitionCourseIdName());
            ps.setString   (8,  competition.getCompetitionGender());
            ps.setString   (9,  competition.getCompetitionGame());
            ps.setShort    (10, competition.getCompetitionStartHole());
            ps.setShort    (11, competition.getFlightNumberPlayers());
            ps.setString   (12, competition.getTimeSlots());
            ps.setString   (13, json);                                             // CompetitionJson
            ps.setString   (14, competition.getCompetitionQualifying());
            ps.setTime     (15, Time.valueOf(competition.getPriceGivingTime()));
            ps.setTimestamp(16, Timestamp.valueOf(competition.getStartingListDate()));
            ps.setTimestamp(17, Timestamp.valueOf(competition.getClassmentDate()));
            ps.setShort    (18, (short) 72);                                       // CompetitionPar
            ps.setString   (19, "0");                                              // CompetitionStatus — initial value
            ps.setShort    (20, competition.getCompetitionAgeLadies());
            ps.setShort    (21, competition.getCompetitionAgeMens());
            ps.setShort    (22, competition.getCompetitionMaximumPlayers());
            ps.setTimestamp(23, Timestamp.from(Instant.now()));                    // ModificationDate
            sql.PrintWarnings.print(ps.getWarnings(), methodName);
            utils.LCUtil.logps(ps);
            return ps;
        } catch (Exception e) {
            handleGenericException(e, methodName);
            return null;
        }
    } // end method

    /*
    void main() {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
    } // end main
    */

} // end class

package rowmappers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import entite.CompetitionDescription;
import static exceptions.LCException.handleGenericException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Time;
import static utils.LCUtil.getCurrentMethodName;

public class CompetitionDescriptionRowMapper extends AbstractRowMapper<CompetitionDescription> {

    private static final ObjectMapper OBJECT_MAPPER;
    static {
        OBJECT_MAPPER = new ObjectMapper();
        OBJECT_MAPPER.registerModule(new JavaTimeModule());
        OBJECT_MAPPER.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
    }

    @Override
    public CompetitionDescription map(ResultSet rs) throws SQLException {
        try {
            CompetitionDescription c = new CompetitionDescription();
            c.setCompetitionId(getInteger(rs, "CompetitionId"));
            c.setCompetitionDate(getLocalDateTime(rs, "CompetitionDate"));
            c.setCompetitionName(getString(rs, "CompetitionName"));
            c.setStartInscriptionDate(getLocalDateTime(rs, "CompetitionStartInscription"));
            c.setEndInscriptionDate(getLocalDateTime(rs, "CompetitionEndInscription"));
            c.setCompetitionClubId(getInteger(rs, "CompetitionClubId"));
            String courseIdName = getString(rs, "CompetitionCourseIdName");
            c.setCompetitionCourseIdName(courseIdName);
            if (courseIdName != null) {
                String s = courseIdName.substring(0, courseIdName.lastIndexOf("-") - 1);
                c.setCompetitionCourseId(Integer.parseInt(s.trim()));
            }
            c.setCompetitionGender(getString(rs, "CompetitionGender"));
            c.setCompetitionGame(getString(rs, "CompetitionGame"));
            c.setCompetitionStartHole(getShort(rs, "CompetitionStartHole"));
            c.setFlightNumberPlayers(getShort(rs, "CompetitionFlightNumberPlayers"));
            c.setTimeSlots(getString(rs, "CompetitionTimeSlots"));
            String handicapJson = getString(rs, "CompetitionHandicapLimitsJson");
            if (handicapJson != null) {
                CompetitionDescription cd = OBJECT_MAPPER.readValue(handicapJson, CompetitionDescription.class);
                c.setSeriesHandicap(cd.getSeriesHandicap());
            }
            c.setCompetitionQualifying(getString(rs, "CompetitionQualifying"));
            Time prizeTime = rs.getTime("CompetitionPrizeGivingTime");
            c.setPriceGivingTime(prizeTime != null ? prizeTime.toLocalTime() : null);
            c.setStartingListDate(getLocalDateTime(rs, "CompetitionStartingListDate"));
            c.setClassmentDate(getLocalDateTime(rs, "CompetitionClassmentDate"));
            c.setCompetitionStatus(getString(rs, "CompetitionStatus"));
            c.setCompetitionPar(getShort(rs, "CompetitionPar"));
            c.setCompetitionAgeLadies(getShort(rs, "CompetitionAgeLadies"));
            c.setCompetitionAgeMens(getShort(rs, "CompetitionAgeMens"));
            c.setCompetitionMaximumPlayers(getShort(rs, "CompetitionMaximumPlayers"));
            return c;
        } catch (Exception e) {
            handleGenericException(e, getCurrentMethodName());
            return null;
        }
    } // end map

} // end class

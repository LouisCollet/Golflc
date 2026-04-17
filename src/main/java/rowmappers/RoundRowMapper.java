
package rowmappers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import entite.Club;
import entite.Round;
import entite.ScoreMatchplay;
import static exceptions.LCException.handleGenericException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

public class RoundRowMapper extends AbstractRowMapper<Round> implements RowMapperRound<Round> {

    private static final ObjectMapper OBJECT_MAPPER;

    static {
        OBJECT_MAPPER = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
        OBJECT_MAPPER.registerModule(new JavaTimeModule());
        OBJECT_MAPPER.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
    }

    @Override
    public Round map(ResultSet rs) throws SQLException {
        // Par défaut, club null
        return map(rs, null);
    }

    public Round map(ResultSet rs, Club club) throws SQLException {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        try {
            Round round = new Round();
            round.setIdround(rs.getInt("idround"));

            if (club == null) {
                club = new Club();
                club.getAddress().setZoneId("Europe/Brussels");
            }

            LocalDateTime ldt = getLocalDateTime(rs, "roundDate");
            ZonedDateTime zdt = ldt.atZone(ZoneId.of("UTC"))
                                     .withZoneSameInstant(ZoneId.of(club.getAddress().getZoneId()));
            round.setRoundDate(zdt.toLocalDateTime());
            round.setRoundDateZoned(zdt);

            round.setRoundGame(getString(rs, "roundgame"));
            round.setRoundCBA(getShort(rs,"RoundCSA"));
            round.setRoundName(getString(rs, "RoundName"));
            round.setRoundQualifying(getString(rs, "RoundQualifying"));
            round.setRoundHoles(getShort(rs,"RoundHoles"));
            round.setRoundStart(getShort(rs,"RoundStart"));

            String mpJson = getString(rs, "RoundMatchplayResult");
            if (!"no MP score".equals(mpJson)) {
                round.setScoreMatchplay(OBJECT_MAPPER.readValue(mpJson, ScoreMatchplay.class));
            }

            round.setRoundTeam(getString(rs, "roundTeam"));
            round.setCourseIdcourse(getInteger(rs,"course_idcourse"));
            round.setRoundCompetition(getString(rs, "RoundCompetition"));

            return round;

        } catch (Exception e) {
            handleGenericException(e, methodName);
            return null;
        }
    }
}


/*
import com.fasterxml.jackson.databind.ObjectMapper;
import entite.Club;
import entite.Round;
import entite.ScoreMatchplay;
import static exceptions.LCException.handleGenericException;
import static interfaces.Log.LOG;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

public class RoundRowMapper implements RowMapperRound<Round> {
 
    @Override
   public Round map(ResultSet rs, Club club) throws SQLException {
       final String methodName = utils.LCUtil.getCurrentMethodName();  
   try{  
           //LOG.debug("entering map for method = " + methodName);
        Round round = new Round();
 //        LOG.debug("entering mapRound with club = " + club);
        round.setIdround(rs.getInt("idround"));
        if(club == null){
            LOG.debug("club == null ==> ZoneID forced to Europe/Brussels");
            club = new Club();
            club.getAddress().setZoneId("Europe/Brussels");
        }
        LocalDateTime ldt = rs.getTimestamp("roundDate").toLocalDateTime();
 //          LOG.debug("ldt roundDate from DB = " + ldt);
        ZoneOffset zoneOffSet = ZoneId.of(club.getAddress().getZoneId()).getRules().getOffset(ldt);
 //          LOG.debug("zoneOffset = " + zoneOffSet); 
// here is the magic !!   08-05-2024   current --> destination, contrepartie = CreateRound on stocke en DB sous format UTC !!!
// ici on restore à l'heure du club où se joue la partie !!
// ATTENTION !!!
        ZonedDateTime zdt = ldt.atZone(ZoneId.of("UTC")).withZoneSameInstant(ZoneId.of(club.getAddress().getZoneId()));
 //           LOG.debug("zdt = " + zdt + " ,offset = " + zdt.getOffset());
        round.setRoundDate(zdt.toLocalDateTime());
  //          LOG.debug("ldt final = " + round.getRoundDate());
        round.setRoundDateZoned(zdt);
  //            LOG.debug("RoundDateZoned = " + round.getRoundDateZoned());
        round.setRoundGame(rs.getString("roundgame"));
        round.setRoundCBA(rs.getShort("RoundCSA"));
        round.setRoundName(rs.getString("RoundName"));
        round.setRoundQualifying(rs.getString("RoundQualifying"));
        
        round.setRoundHoles(rs.getShort("RoundHoles"));
        round.setRoundStart(rs.getShort("RoundStart"));
     if(!rs.getString("RoundMatchplayResult").equals("no MP score")){ // il y a des infos matchplay
          ObjectMapper om = new ObjectMapper();
          ScoreMatchplay mp = om.readValue(rs.getString("RoundMatchplayResult"),ScoreMatchplay.class);
          round.setScoreMatchplay(mp);
//          LOG.debug("r.setScoreMatchplay = " + r.getScoreMatchplay());
     }
        round.setRoundTeam(rs.getString("roundTeam"));
        round.setCourseIdcourse(rs.getInt("course_idcourse"));
        round.setRoundCompetition(rs.getString("RoundCompetition"));
        
   return round;
 }catch(Exception e){
        handleGenericException(e, methodName);
    return null;
 }
} //end method
} // end class*/
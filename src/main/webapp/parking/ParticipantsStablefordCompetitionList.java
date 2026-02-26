package lists;

import entite.Classment;
import entite.Club;
import entite.CompetitionDescription;
import entite.Course;

import entite.HandicapIndex;
import entite.Inscription;
import entite.Player;
import entite.Round;
import static exceptions.LCException.handleGenericException;
import static exceptions.LCException.handleSQLException;
import static interfaces.Log.LOG;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import rowmappers.ClubRowMapper;
import rowmappers.CourseRowMapper;
import rowmappers.InscriptionRowMapper;
import rowmappers.PlayerRowMapper;
import rowmappers.RoundRowMapper;
import rowmappers.RowMapper;
import rowmappers.RowMapperRound;
import connection_package.DBConnection;
import entite.composite.ECourseList;
import rowmappers.ClassmentRowMapper;
import rowmappers.HandicapIndexRowMapper;
import utils.LCUtil;

public class ParticipantsStablefordCompetitionList implements Serializable, interfaces.Log{
    private static List<ECourseList> liste = null;
    
 
  public List<ECourseList> list(final CompetitionDescription competition ,final Connection conn) throws SQLException, Exception{ 
    final String methodName = utils.LCUtil.getCurrentMethodName();

if(liste == null){
    LOG.debug(" ... entering " + methodName);
    LOG.debug(" with Competition Description = " + competition);
    PreparedStatement ps = null;
    ResultSet rs = null;
try{

    final String query = """
       SELECT *
       FROM player
       JOIN competition_description
          ON CompetitionId = ?
       JOIN round
           ON round.RoundName = competition_description.CompetitionName
       JOIN course
           ON round.course_idcourse = course.idcourse
       JOIN club
           ON course.club_idclub = club.idclub
       JOIN player_has_round
           ON  InscriptionIdPlayer = player.idplayer
           AND InscriptionIdRound = round.idround
       ORDER by player_has_round.InscriptionFinalResult DESC
    """;

     ps = conn.prepareStatement(query);
     ps.setInt(1, competition.getCompetitionId()); //.getCompetitionName());

      utils.LCUtil.logps(ps);
    rs =  ps.executeQuery();
    liste = new ArrayList<>();
    int i = 0;
    RowMapper<Club> clubMapper = new ClubRowMapper();
    RowMapper<Course> courseMapper = new CourseRowMapper();
    RowMapper<Player> playerMapper = new PlayerRowMapper();
    RowMapper<Inscription> inscriptionMapper = new InscriptionRowMapper();
    RowMapperRound<Round> roundMapper = new RoundRowMapper(); // accepte rs et 
    RowMapper<HandicapIndex> handicapIndexMapper = new HandicapIndexRowMapper();
    RowMapper<Classment> classmentMapper = new ClassmentRowMapper();
    
//    club = clubMapper.map(rs);
	while(rs.next()){
            i++;
            Player player = playerMapper.map(rs);
            Club club = clubMapper.map(rs);
            Round round = roundMapper.map(rs, club);
            Classment classment  = new read.ReadClassment().read(player, round, conn); 

            HandicapIndex handicapIndex = new HandicapIndex();
            handicapIndex.setHandicapPlayerId(player.getIdplayer());
            handicapIndex.setHandicapDate(round.getRoundDate());
            handicapIndex = new find.FindHandicapIndexAtDate().find(handicapIndex, conn);
            ECourseList ecl = ECourseList.builder()
                    .club(club)
                    .course(courseMapper.map(rs))
                    .handicapIndex(handicapIndexMapper.map(rs))
                    .inscription(inscriptionMapper.map(rs))
                    .round(round)
                    .player(player)
                    .classment(classmentMapper.map(rs))
            .build();
            
            
         // ECourseList ecl = new ECourseList();
      //    ecl.setClub(clubMapper.map(rs));
        //  ecl.setCourse(courseMapper.map(rs));
         // ecl.setRound(new entite.Round().dtoMapper(rs,ecl.getClub()));// mod 19-02-2020 pour générer ZonedDateTime
        //  ecl.setRound(roundMapper.map(rs, ecl.getClub()));
        //  ecl.setInscription(inscriptionMapper.map(rs));
        //  ecl.setPlayer(playerMapper.map(rs));
       // et si erreur ? cla = null
          
        //  ecl.setClassment(cla);
       //   HandicapIndex handicapIndex = new HandicapIndex();
        //  handicapIndex.setHandicapPlayerId(ecl.player().getIdplayer());
        //  handicapIndex.setHandicapDate(ecl.round().getRoundDate());
        //  handicapIndex = new find.FindHandicapIndexAtDate().find(handicapIndex, conn);
       //   ecl.setHandicapIndex(handicapIndex);
	liste.add(ecl);
	} // end while
  if(i == 0){
       String msg = "££ Empty Result List in " + methodName;
       LOG.error(msg);
       LCUtil.showMessageFatal(msg);
 //      return null;
  }else{
         LOG.debug("ResultSet " + methodName + " has " + liste.size() + " lines.");
   }
    LOG.debug("ending liste  =  ",  Arrays.deepToString(liste.toArray()) );
    
// https://stackoverflow.com/questions/369512/how-to-compare-objects-by-multiple-fields
  //LOG.debug
  //liste.forEach(item -> LOG.debug("liste before sort " + item));
  liste.forEach(item -> LOG.debug("liste BEFORE sort " + NEW_LINE + item.player().getPlayerLastName()+ item.classment().getTotalPoints()));
/* bug !!
  liste.sort(Comparator.comparingInt((ECourseList p)->p.Eclassment.getTotalPoints() ).reversed()  // descending
   //        liste.sort(Comparator.comparingInt((ECourseList p)->p.Eclassment.getLast3() ).reversed()
               //    .thenComparing((ECourseList p)->p.Eclassment.getTotalPoints() )
              .thenComparingInt((ECourseList p)->p.Eclassment.getLast9() ).reversed()
              .thenComparingInt((ECourseList p)->p.Eclassment.getLast6() ).reversed()
                 .thenComparingInt((ECourseList p)->p.Eclassment.getLast3() ).reversed()
                .thenComparingInt((ECourseList p)->p.Eclassment.getLast1() ).reversed()
            );
  liste.forEach(item -> LOG.debug("liste AFTER sort1 " + NEW_LINE + item.Eplayer.getPlayerFirstName()+ item.Eclassment));
  */
   // attention au au -p
   ///https://stackoverflow.com/questions/51565422/java-8-compare-multiple-fields-in-different-order-using-comparator
   //There is no need to use the method Comparator::reverse. 
   //Since you want to reverse the comparison based on the integer, just negate the age -p.getAge()
// and it will be sorted in the descending order:
    liste.sort(Comparator.comparingInt((ECourseList p)-> p.classment().getTotalPoints() ).reversed()  // -p=descending trick !!!
                     .thenComparingInt((ECourseList p)-> p.classment().getLast9() ).reversed()
                     .thenComparingInt((ECourseList p)-> p.classment().getLast6() ).reversed()
                     .thenComparingInt((ECourseList p)-> p.classment().getLast3() ).reversed()
                     .thenComparingInt((ECourseList p)-> p.classment().getLast1() ).reversed()
            );

 //  liste.sort(Comparator.comparing((ECourseList p)-> p.getClassment().getTotalPoints()).reversed());// p)-> -p.getClassment().getTotalPoints() ));
    
 //  .thenComparingInt((ECourseList p)-> -p.getClassment().getLast1() )
    
     liste.forEach(item -> LOG.debug("liste AFTER sort = " + NEW_LINE + item.player().getPlayerFirstName() + " / "
                                            + item.player().getIdplayer() + " /" + item.classment()));
    return liste;
}catch (NullPointerException npe){ 
    String msg = "NullPointerException in " + methodName + npe;
    LOG.error(msg);
    LCUtil.showMessageFatal(msg);
    return null;
}catch (SQLException e){
        handleSQLException(e, methodName);
        return null;
}catch (Exception e){
    handleGenericException(e, methodName);
    return null;
}finally{
        DBConnection.closeQuietly(null, null, rs, ps); // new 14/08/2014
}
}else{
   //    LOG.debug("escaped to listParticipants repetition with lazy loading");
    return liste;  //plusieurs fois ??
    }
} //end method

    public static List<ECourseList> getListe() {
        return liste;
    }
    public static void setListe(List<ECourseList> liste) {
        ParticipantsStablefordCompetitionList.liste = liste;
    }

 void main() throws SQLException, Exception {// testing purposes
    Connection conn = new DBConnection().getConnection();
    CompetitionDescription cde = new CompetitionDescription();
    
 //  cde.setCompetitionName("Competition de test 24");
   cde.setCompetitionId(24);
   List<ECourseList> ecl = new ParticipantsStablefordCompetitionList().list(cde, conn);
     LOG.debug("résultat : " + ecl.getFirst()); // was get(0)
   Classment c = new Classment();
      LOG.debug("Inscription list = " + ecl.toString());
     ecl.forEach(item -> LOG.debug("Participants Stableford list = " + item.classment() + "/" + item.player().getPlayerLastName()));  // java 8 lambda                     
   DBConnection.closeQuietly(conn, null, null, null);
}// end main
} //end class
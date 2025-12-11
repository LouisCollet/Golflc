package lists;

import entite.composite.ECourseList;
import entite.Player;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import utils.DBConnection;
import utils.LCUtil;

public class PlayedList implements interfaces.Log{
     private static List<ECourseList> liste = null;
    private final static String CLASSNAME = utils.LCUtil.getCurrentClassName();
public List<ECourseList> list(final Player player, final Connection conn) throws SQLException{

    final String methodName = utils.LCUtil.getCurrentMethodName(CLASSNAME); 
if(liste == null){
        LOG.debug("starting PlayedList(), Player = {}", player.getIdplayer());
        LOG.debug("with player = " + player.toString());
    PreparedStatement ps = null;
    ResultSet rs = null;
try{

 String query = """
WITH selection AS (
      SELECT * from player_has_round, round, player
      WHERE player.idplayer = ?
        AND player_has_round.InscriptionIdPlayer = player.idplayer
        AND player_has_round.InscriptionIdRound = round.idround
     )
 SELECT * FROM selection
    JOIN tee
        ON tee.idtee = selection.InscriptionIdTee
    JOIN course
        ON course.idcourse = selection.course_idcourse
    JOIN club
        ON club.idclub = course.club_idclub
    ORDER BY selection.RoundDate DESC
    LIMIT 30;
""";
 
//154 2024-01-19T09:23:11,223 215614  ERROR lists.PlayedList . list 75 :
//SQL Exception in getPlayedList() = java.sql.SQLSyntaxErrorException: Expression #45 of 
//SELECT list is not in GROU P BY clause and contains nonaggregated column 'golflc.tee.TeeGender' which is not functionally dependent on columns in GROU P BY clause;
//this is incompatible with sql_mode=only_full_group_by, SQLState = 42000, ErrorCode = 1055 

        ps = conn.prepareStatement(query);
        ps.setInt(1, player.getIdplayer());
        utils.LCUtil.logps(ps);
	rs =  ps.executeQuery();
        liste = new ArrayList<>();
	while(rs.next()){
          ECourseList ecl = new ECourseList();
          ecl.setClub(entite.Club.dtoMapper(rs));
          ecl.setCourse(entite.Course.dtoMapper(rs));
          ecl.setRound(new entite.Round().dtoMapper(rs,ecl.getClub()));// mod 19-02-2020 pour générer ZonedDateTime
          ecl.setTee(entite.Tee.dtoMapper(rs));
          ecl.setInscription(entite.Inscription.map(rs));
      //    ecl.setScoreStableford(entite.ScoreStableford.map(rs));
	liste.add(ecl);
	} //end while
     if(liste.isEmpty()){
         String msg = "££ Empty Result Table in " + methodName;
         LOG.error(msg);
         LCUtil.showMessageFatal(msg);
  //       return null;
     }else{
         LOG.debug("ResultSet " + methodName + " has " + liste.size() + " lines.");
     }
     //     liste.forEach(item -> LOG.debug("PlayedList " + item));  // java 8 lambda
    return liste;
    
}catch (SQLException e){
    String msg = "SQL Exception in getPlayedList() = " + e.toString() + ", SQLState = " + e.getSQLState()
            + ", ErrorCode = " + e.getErrorCode();
	LOG.error(msg);
        LCUtil.showMessageFatal(msg);
        return null;
}catch (Exception ex){
    LOG.error("Exception ! " + ex);
    LCUtil.showMessageFatal("Exception getPlayedList= " + ex.toString() );
     return null;
}finally{
        DBConnection.closeQuietly(null, null, rs, ps); // new 14/08/2014
}
}else{
   //      LOG.debug("escaped to listPlayed repetition with lazy loading");
    return liste;  //plusieurs fois ??
}

} //end method

    public static List<ECourseList> getListe() {
        return liste;
    }

    public static void setListe(List<ECourseList> liste) {
        PlayedList.liste = liste;
    }
    
    void main() throws SQLException, Exception {
      Connection conn = new DBConnection().getConnection(); 
  try{
    // player n'est plus utilisé au 19-09-2021 pour enregistrement des résultats de flights dans lequel LC ne se trouve pas
    Player player = new Player();
    player.setIdplayer(324720);
 //   player = new load.LoadPlayer().load(player, conn);
    List<ECourseList> ecl = new PlayedList().list(player, conn);
    for(ECourseList f : ecl) {
        if(f.getRound().getIdround() == 688) {
            LOG.debug(NEW_LINE + "Main ecl found = " + f); //.toString());
   //         LOG.debug("Round found = " + customer.getRound()); //.toString());
        }
        
    }
 //       LOG.debug("from main, after lp = " + lp);
   }catch (Exception e){
            String msg = "Â£Â£ Exception in main = " + e.getMessage();
            LOG.error(msg);
   }finally{
         DBConnection.closeQuietly(conn, null, null , null); 
          }
   } // end main//
} //end Class
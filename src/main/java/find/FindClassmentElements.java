package find;

import entite.Classment;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import utils.LCUtil;

public class FindClassmentElements implements interfaces.Log, interfaces.GolfInterface
{
    private static Classment liste = null;
    final private static String CLASSNAME = Thread.currentThread().getStackTrace()[1].getClassName(); 
    
public Classment findClassment(int id_player, final int id_round, final Connection conn) throws SQLException{
    
    LOG.info("starting findClassmentElements for idplayer = " + id_player);
    LOG.info("starting findClassmentElements for idround = " + id_round);
//if(liste == null)
//
    PreparedStatement ps = null;
    ResultSet rs = null;
try{
  String query = 
    "SELECT " +
"	score.player_has_round_player_idplayer," +
"	sum(score.ScoreExtraStroke) as TotalExtraStrokes," +
"       sum(score.ScorePoints) as TotalScore," +
"       sum( IF (score.ScoreHole > 9, ScorePoints,0)) as Last9," + // true = sum, false = 0
                                // http://webdevzoom.com/sum-if-function-calculate-field-values-in-mysql/
"       sum( IF (score.ScoreHole > 12,ScorePoints,0)) as Last6," +
"       sum( IF (score.ScoreHole > 15,ScorePoints,0)) as Last3," +
"       sum( IF (score.ScoreHole > 17,ScorePoints,0)) as Last1" +
"  FROM score" +
" 	where score.player_has_round_player_idplayer = ?" +
" 	and score.player_has_round_round_idround = ?" +
"	 ; "
     ;
  //
 //       LOG.info("player = " + id_player) ;
 //       LOG.info("round = " + id_round) ;
    ps = conn.prepareStatement(query);
    ps.setInt(1, id_player);
    ps.setInt(2, id_round);

        utils.LCUtil.logps(ps);
    rs =  ps.executeQuery();
        rs.last(); //on récupère le numéro de la ligne
            LOG.info("ResultSet FindClassmentElements " + rs.getRow() + " lines.");
        if(rs.getRow() > 1){ 
            throw new Exception(" -- More than 1 sum extra strokes = " + rs.getRow() );  }
        rs.beforeFirst(); //on replace le curseur avant la première ligne
  ////      liste = new ArrayList<>();
          //LOG.info("just before while ! ");
  //      int t = 0;
  //  liste = new ArrayList<>();    
	while(rs.next()) {
            
  // à faire           c = entite.Classment.mapClassment(rs);
   //  liste.add(entite.Classment.mapClassment(rs));          
             Classment c = new Classment();
              c.setTotalExtraStrokes(rs.getInt("TotalExtraStrokes"));
              c.setTotalPoints(rs.getInt("TotalScore")); 
              c.setLast9(rs.getInt("Last9"));
              c.setLast6(rs.getInt("Last6"));
              c.setLast3(rs.getInt("Last3"));
              c.setLast1(rs.getInt("Last1"));
              
             liste = c; //dd(c); 
	}
        LOG.info("exiting with Classment = "  + liste.toString());
        return liste;
}catch (SQLException e){
    String msg = "SQL Exception = " + e.toString() + ", SQLState = " + e.getSQLState()
            + ", ErrorCode = " + e.getErrorCode();
	LOG.error(msg);
        LCUtil.showMessageFatal(msg);
        return null;
}catch (NullPointerException npe){   
    String msgerr = "NullPointerException in : " + CLASSNAME + " " + npe;
        LOG.error(msgerr);
    LCUtil.showMessageFatal(msgerr);
     return null;
}catch (Exception ex){
    String msg = "Exception in FindTotalExtraStrokes()" + ex;
    LOG.error(msg);
    LCUtil.showMessageFatal(msg);
     return null;
}finally{
    utils.DBConnection.closeQuietly(null, null, rs, ps); // new 14/08/2014
}
//}else{
////         LOG.debug("escaped to findClassmentElements repetition thanks to lazy loading");
//    return liste;  //not null, donc pas d'acces
      //   return liste;
////         return null;

//} // 
}//end method

public static void main(String[] args) throws SQLException, Exception{
    LOG.info("Input main = " );
 //   DBConnection dbc = new DBConnection();
//Connection conn = dbc.getConnection();
 Connection conn = new utils.DBConnection().getConnection();

//    Player player = new Player();
//    Round round =new Round(); 
//player.setIdplayer(324713);
//round.setIdround(260);
   FindClassmentElements ftes = new FindClassmentElements();
   ftes.findClassment(324713,260, conn);

utils.DBConnection.closeQuietly(conn, null, null, null);

}// end main
} // end Class
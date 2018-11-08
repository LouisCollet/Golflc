
package delete;

import entite.ECourseList;
import entite.Player;
import entite.Round;
import static interfaces.Log.LOG;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import utils.DBConnection;
import utils.LCUtil;

public class DeleteInscription implements interfaces.Log, interfaces.GolfInterface
{
  //  public int deleteInscription(final Player player, final Round round, final ClubCourseRound ccr,Connection conn) throws Exception {
public int deleteInscription(final Player player, final Round round, final ECourseList ecl,Connection conn) throws Exception {
        PreparedStatement ps = null;     // a modifier pour tenir compte du round, sinon delete de tous les round !
                // il faut aussi modifier le nombre de joueurs inscrits dans RoundPlayers !!!
try
{   //encore Ã  faire : delete du record activation s'il existe ...
         LOG.info("starting delete for inscription ... = " );
         LOG.info("for player id  = " + player.getIdplayer() );
         LOG.info("for player last name= " + player.getPlayerLastName() );
        LOG.info("for round = " + round.getIdround() );
        
      find.FindCountScore sciu = new find.FindCountScore();
     int rows = sciu.getCountScore(conn, player, round, "rows");
       if (rows == 99)
       {  LOG.error("Fatal error in getcountscore/count rows");
//           throw new Exception(" -- Fatal error in getCountStore, score = " + rows);
       }
       if (rows == 0) // le score n'est pas encore enregistré
       {
           LOG.info(" OK -- Score pas encore enregistré  ! ");
          
       }else{
              LOG.info(" -- score enregistré, delete refised rows =  " + rows);
              return 0;
       }

    String query = " DELETE" +
        " from player_has_round" +
        " WHERE player_has_round.player_idplayer = ?" +
        " AND player_has_round.round_idround     = ?";
    ps = conn.prepareStatement(query); 
    ps.setInt(1, player.getIdplayer());
    ps.setInt(2, round.getIdround());
    LCUtil.logps(ps); 
    rows = ps.executeUpdate();
        LOG.info("deleted inscription = " + rows);
if (rows == 0) // no delete !!
   {
    String msg =  LCUtil.prepareMessageBean("inscription.not.canceled");
    msg = msg       + "<br/>player id = " + player.getIdplayer()
                    + " <br/>Player Last Name = " + player.getPlayerLastName()
                    + " <br/>Round id = " + round.getIdround();
       LOG.info(msg);
    LCUtil.showMessageInfo(msg);
    return 0;
}else{ // row deleted
    String msg =  LCUtil.prepareMessageBean("inscription.canceled");
    msg = msg       + " <br/>player id = " + player.getIdplayer()
                    + " <br/></h1>player Last Name = " + player.getPlayerLastName()
                    + " <br/></h1>round id = " + round.getIdround();
           LOG.info(msg);
    LCUtil.showMessageInfo(msg);
 //   LOG.info("line 01");
    String sujet = "Cancellation of your Round Inscription in GolfLC";
    String mail =
                  " <br/>Annulation Confirmation - GolfLC!"
                + " <br/>" + SDF_TIME.format(new java.util.Date() )
                + " <br/> Round Game   = " + round.getRoundGame()
                + " <br/> Round Date   = " + round.getRoundDate().format(ZDF_TIME_HHmm)
                + " <br/> Course Name  = " + ecl.Ecourse.getCourseName()
                + " <br/> Club Name    = " + ecl.Eclub.getClubName()
                + " <br/> Club City    = " + ecl.Eclub.getClubCity()
                + " <br/><b>ID         = </b>" + player.getIdplayer()
                + " <br/><b>First Name = </b>" + player.getPlayerFirstName()
                + " <br/><b>Last Name  = </b>" + player.getPlayerLastName()
                + " <br/><b>Language   = </b>" + player.getPlayerLanguage()
                + " <br/><b>City       = </b>" + player.getPlayerCity()
                + " <br/><b>Email      = </b>" + player.getPlayerEmail()
                + " <br/> Thank you !"
                + " <br/> The GolfLC team"
                    ; 
        
    String to = "louis.collet@skynet.be";
    utils.SendEmail sm = new utils.SendEmail();
    boolean b = sm.sendHtmlMail(sujet,mail,to,"DELETE INSCRIPTION");
       LOG.info("HTML Mail status = " + b);
        
    return rows;
}
/*    
        
  String query = " DELETE from score where score.player_has_round_player_idplayer = ?";
    ps = conn.prepareStatement(query); 
    ps.setInt(1, player.getIdplayer());
    LCUtil.logps(ps); 
    int row_score = ps.executeUpdate();
        LOG.info("deleted score = " + row_score);
        
        
        
        
    query = " delete from handicap where handicap.player_idplayer = ?";
    ps = conn.prepareStatement(query); 
    ps.setInt(1, idplayer);
    LCUtil.logps(ps); 
    int row_hcp = ps.executeUpdate();
        LOG.info("deleting handicap = " + row_hcp);
    
    query = " delete from player where player.idplayer = ?";
    ps = conn.prepareStatement(query); 
    ps.setInt(1, idplayer);
    LCUtil.logps(ps); 
    int row_player = ps.executeUpdate();
        LOG.info("deleting player = " + row_player);
   */ 


  //  String msg = "<br/> <h1>Records deleted = " 
                  //      + " <br/></h1>player = " + idplayer
        //                + " <br/>score = " + row_score
        //                + " <br/>inscription = " + row_phr;
     //                   + " <br/>handicap = " + row_hcp
       //                 + " <br/>player = " + row_player;
  //         LOG.info(msg);
  //      LCUtil.showMessageInfo(msg);
  //      return "Inscription deleted ! ";

}catch (SQLException e){
    String msg = "SQL Exception in DeleteInscription = " + e.toString() + ", SQLState = " + e.getSQLState()
            + ", ErrorCode = " + e.getErrorCode();
    LOG.error(msg);
    LCUtil.showMessageFatal(msg);
    return 0;
}catch (Exception ex){
    String msg = "Exception in DeleteInscription() " + ex;
    LOG.error(msg);
    LCUtil.showMessageFatal(msg);
    return 0;
}finally{
        utils.DBConnection.closeQuietly(null, null, null, ps);
}
} //end method

    
    
    
    
 public static void main(String[] args) 
 {
 try{
       LOG.info("Input main = ");
       DBConnection dbc = new DBConnection();
    Connection conn = dbc.getConnection();
    Player player = new Player();
    Round round =new Round(); 
    ECourseList ecl = new ECourseList();
    player.setIdplayer(324733);
    round.setIdround(323);
    DeleteInscription di  = new DeleteInscription();
    di.deleteInscription(player,round, ecl, conn);
    DBConnection.closeQuietly(conn, null, null, null);
 } catch (Exception e) {
            String msg = "Â£Â£ Exception in main = " + e.getMessage();
            LOG.error(msg);
      //      LCUtil.showMessageFatal(msg);
   }finally{
     //    DBConnection.closeQuietly(null, stm, rs, null); 
          }
   } // end method main
} //end class
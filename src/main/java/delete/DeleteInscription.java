
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
 public boolean deleteInscription(final Player player, final Round round, final ECourseList ecl, Connection conn) throws Exception {
        PreparedStatement ps = null;     // a modifier pour tenir compte du round, sinon delete de tous les round !
                // il faut aussi modifier le nombre de joueurs inscrits dans RoundPlayers !!!
try
{   //encore Ã  faire : delete du record activation s'il existe ...
         LOG.info("starting delete for inscription ... = " );
         LOG.info("for player id  = " + player.getIdplayer() );
         LOG.info("for player last name= " + player.getPlayerLastName() );
         LOG.info("for round = " + round.getIdround() );
         LOG.info("for round 2 = " + ecl.Eround.getIdround() );
        
   //   find.FindCountScore sciu = new find.FindCountScore();
      int rows = new find.FindCountScore().getCountScore(conn, player, round, "rows");
       if (rows == 99){
           LOG.error("Fatal error in getcountscore/count rows");
           return false;
//           throw new Exception(" -- Fatal error in getCountStore, score = " + rows);
       }
       if (rows == 0){ // le score n'est pas encore enregistré
           LOG.info(" OK -- Score pas encore enregistré  ! ");
       }else{
           String msg = " -- score enregistré : delete refused rows =  " + rows;
              LOG.info(msg);
              LCUtil.showMessageFatal(msg);
              return false;
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
    if (rows == 0){ // no delete !!
        String msg =  LCUtil.prepareMessageBean("inscription.not.canceled");
        msg = msg   + "<br/>player id = " + player.getIdplayer();
        msg = msg   + "<br/>Player Last Name = " + player.getPlayerLastName();
        msg = msg   + "<br/>Round id = " + round.getIdround();
        LOG.info(msg);
        LCUtil.showMessageInfo(msg);
        return false;
}else{ // row deleted
    String msg =  LCUtil.prepareMessageBean("inscription.canceled")
        + " <br/>Player id = " + player.getIdplayer()
        + " <br/>Player Last Name = " + player.getPlayerLastName()
        + " <br/>Round id = " + round.getIdround();
      LOG.info(msg);
    LCUtil.showMessageInfo(msg);
    mail.DeleteInscriptionMail mdi = new mail.DeleteInscriptionMail();
    mdi.sendMail(player, round, ecl.Eclub, ecl.Ecourse);
    return true;
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
    return false;
}catch (Exception ex){
    String msg = "Exception in DeleteInscription() " + ex;
    LOG.error(msg);
    LCUtil.showMessageFatal(msg);
    return false;
}finally{
        utils.DBConnection.closeQuietly(null, null, null, ps);
}
} //end method

 public static void main(String[] args){
 try{
       LOG.info("Input main = ");
   //    DBConnection dbc = new DBConnection();
    Connection conn = new DBConnection().getConnection();
    Player player = new Player();
    Round round =new Round(); 
    ECourseList ecl = new ECourseList();
    player.setIdplayer(324733);
    round.setIdround(323);
  //  DeleteInscription di  = new DeleteInscription();
    new DeleteInscription().deleteInscription(player,round, ecl, conn);
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

package delete;

import entite.Club;
import entite.Course;
import entite.Player;
import entite.Round;
import static interfaces.Log.LOG;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import utils.DBConnection;
import utils.LCUtil;
import static utils.LCUtil.showMessageFatal;

public class DeleteInscription implements interfaces.Log, interfaces.GolfInterface{
 public boolean delete(final Player player, final Round round, Club club, Course course,Connection conn) throws Exception {
        PreparedStatement ps = null;     // a modifier pour tenir compte du round, sinon delete de tous les round !
                // il faut aussi modifier le nombre de joueurs inscrits dans RoundPlayers !!!
try{   //encore Ã  faire : delete du record activation s'il existe ...
         LOG.debug("starting delete for inscription ... = " );
         LOG.debug("for player  = " + player.toString() );
         LOG.debug("for player last name= " + player.getPlayerLastName() );
         LOG.debug("for round = " + round.toString() );
   //      LOG.debug("for club = " + club.toString() );
   //      LOG.debug("for course = " + course.toString() );
 
      int rows = new find.FindCountScore().find(conn, player, round, "rows");
       if (rows == 99){
           LOG.error("Fatal error in getcountscore/count rows");
           return false;
//           throw new Exception(" -- Fatal error in getCountStore, score = " + rows);
       }
       if (rows == 0){ // le score n'est pas encore enregistré
            LOG.debug(" OK -- Score pas encore enregistré  ! ");
       }else{
            String msg = " -- score enregistré : delete refused rows =  " + rows;
            LOG.debug(msg);
            showMessageFatal(msg);
            return false;
       }

    String query = """
        DELETE
        from player_has_round
        WHERE InscriptionIdPlayer = ?
        AND InscriptionIdRound = ?
     """;
    ps = conn.prepareStatement(query); 
    ps.setInt(1, player.getIdplayer());
    ps.setInt(2, round.getIdround());
    LCUtil.logps(ps); 
    rows = ps.executeUpdate();
        LOG.debug("deleted inscription = " + rows);
    if (rows == 0){ // no delete !!
        String msg =  LCUtil.prepareMessageBean("inscription.not.canceled");
        msg = msg   + "<br/>player id = " + player.getIdplayer();
        msg = msg   + "<br/>Player Last Name = " + player.getPlayerLastName();
        msg = msg   + "<br/>Round id = " + round.getIdround();
        LOG.debug(msg);
        LCUtil.showMessageInfo(msg);
        return false;
   }else{ // row deleted
        String msg =  LCUtil.prepareMessageBean("inscription.canceled")
         + " <br/>Player id = " + player.getIdplayer()
         + " <br/>Player Last Name = " + player.getPlayerLastName()
         + " <br/>Round id = " + round.getIdround();
      // à faire : modify round.roundPlayers
        LOG.debug(msg);
        LCUtil.showMessageInfo(msg);
        // à fair modify round?roundPlayers (nombre de joueurs du flight
        
    query = """
        DELETE
        FROM payments_greenfee
        WHERE GreenfeeIdPlayer = ?
        AND GreenfeeIdRound = ?
      """ ;
    ps = conn.prepareStatement(query); 
    ps.setInt(1, player.getIdplayer());
    ps.setInt(2, round.getIdround());
    LCUtil.logps(ps); 
  //  int row_score = ps.executeUpdate();
        LOG.debug("deleted PaymentGreenfee  = " + ps.executeUpdate());
  //      boolean b = new mail.InscriptionMail().delete(player, round, club, course);
        return true;
}
/*    
        
  final String query = " DELETE from score where score.player_has_round_player_idplayer = ?";
    ps = conn.prepareStatement(query); 
    ps.setInt(1, player.getIdplayer());
    LCUtil.logps(ps); 
    int row_score = ps.executeUpdate();
        LOG.debug("deleted score = " + row_score);
        
        
        
        
    query = " delete from handicap where handicap.player_idplayer = ?";
    ps = conn.prepareStatement(query); 
    ps.setInt(1, idplayer);
    LCUtil.logps(ps); 
    int row_hcp = ps.executeUpdate();
        LOG.debug("deleting handicap = " + row_hcp);
    
    query = " delete from player where player.idplayer = ?";
    ps = conn.prepareStatement(query); 
    ps.setInt(1, idplayer);
    LCUtil.logps(ps); 
    int row_player = ps.executeUpdate();
        LOG.debug("deleting player = " + row_player);
   */ 


  //  String msg = "<br/> <h1>Records deleted = " 
                  //      + " <br/></h1>player = " + idplayer
        //                + " <br/>score = " + row_score
        //                + " <br/>inscription = " + row_phr;
     //                   + " <br/>handicap = " + row_hcp
       //                 + " <br/>player = " + row_player;
  //         LOG.debug(msg);
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

public static void main(String args[]) throws SQLException, Exception{     
 try{
       LOG.debug("Input main = ");
    Connection conn = new DBConnection().getConnection();
    Player player = new Player();
    player.setIdplayer(324715);
    Round round = new Round(); 
    round.setIdround(757);
    Club club = new Club();
    Course course = new Course();
    boolean b = new DeleteInscription().delete(player,round,club, course, conn);
    DBConnection.closeQuietly(conn, null, null, null);
 } catch (Exception e) {
            String msg = "Â£Â£ Exception in main = " + e.getMessage();
            LOG.error(msg);
   }finally{
     //    DBConnection.closeQuietly(null, stm, rs, null); 
          }
   } // end method main
} //end class
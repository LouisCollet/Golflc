
package delete;

import entite.Player;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import utils.DBConnection;
import utils.LCUtil;

public class DeletePlayer implements interfaces.Log, interfaces.GolfInterface{
   private final static String CLASSNAME = utils.LCUtil.getCurrentClassName(); 

       public boolean deletePlayerAndChilds(final Player player,final Connection conn) throws Exception{
       final String methodName = utils.LCUtil.getCurrentMethodName(CLASSNAME);
    PreparedStatement ps = null;
try{  
   
        /* encore à faire : payments-cotisation, greenfee, creditcard, activation
    
     prb si player a un PlayerRole admin (local administrateur)
    SQL Exception in delete.DeletePlayer.deletePlayerAndChilds / java.sql.SQLIntegrityConstraintViolationException:
    Cannot delete or update a parent row: a foreign key constraint fails
    (`golflc`.`club`, CONSTRAINT `club_existe_local_admin` FOREIGN KEY (`ClubLocalAdmin`)
    REFERENCES `player` (`idplayer`)), SQLState = 23000, ErrorCode = 1451
    solution insert value null dans ClubLocalAdmin
    */
     LOG.debug("starting " + methodName);
     // on commende par le niveau le plus bas !
  String query = """
               DELETE from score
               WHERE score.player_has_round_player_idplayer = ?
            """;
    ps = conn.prepareStatement(query); 
    ps.setInt(1, player.getIdplayer());
    LCUtil.logps(ps); 
    int row_score = ps.executeUpdate();
        LOG.debug("deleted score = " + row_score);
    
    query = """
             DELETE from player_has_round
             WHERE InscriptionIdPlayer = ?
          """;
    ps = conn.prepareStatement(query); 
    ps.setInt(1, player.getIdplayer());
    LCUtil.logps(ps); 
    int row_inscription = ps.executeUpdate();
        LOG.debug("deleted inscription = " + row_inscription);
    
    query = """
          DELETE from handicap
          WHERE handicap.player_idplayer = ?
         """;
    ps = conn.prepareStatement(query); 
    ps.setInt(1, player.getIdplayer());
    LCUtil.logps(ps); 
    int row_hcp = ps.executeUpdate();
        LOG.debug("deleted handicap EGA = " + row_hcp);
    
    query = """
             DELETE from handicap_index
             WHERE HandicapPlayerId = ?
            """;
    ps = conn.prepareStatement(query); 
    ps.setInt(1, player.getIdplayer());
    LCUtil.logps(ps); 
    int row_hcp_index = ps.executeUpdate();
        LOG.debug("deleted Handicap Index WHS = " + row_hcp_index);

    query = """
            DELETE from blocking
            WHERE BlockingPlayerId = ?
           """;
    ps = conn.prepareStatement(query); 
    ps.setInt(1, player.getIdplayer());
    LCUtil.logps(ps); 
    int row_blocking = ps.executeUpdate();
        LOG.debug("deleted blocking = " + row_blocking);

    query = """
            DELETE from audit
            WHERE AuditPlayerId = ?
            """;
    ps = conn.prepareStatement(query); 
    ps.setInt(1, player.getIdplayer());
    LCUtil.logps(ps); 
    int row_audit = ps.executeUpdate();
        LOG.debug("deleted audit = " + row_audit);
    
    query = """
            DELETE from payments_subscription
            WHERE SubscriptionIdPlayer = ?
          """;
    ps = conn.prepareStatement(query); 
    ps.setInt(1, player.getIdplayer());
    LCUtil.logps(ps); 
    int row_subscription = ps.executeUpdate();
        LOG.debug("deleted subscription = " + row_subscription);
        
    query = """
           DELETE from lesson
           WHERE EventPlayerId = ?
          """;
    ps = conn.prepareStatement(query); 
    ps.setInt(1, player.getIdplayer());
    LCUtil.logps(ps); 
    int row_schedule = ps.executeUpdate();
        LOG.debug("deleted schedule = " + row_schedule);    
        
       
    query = """
            DELETE from player
            WHERE player.idplayer = ?
          """;
    ps = conn.prepareStatement(query); 
    ps.setInt(1, player.getIdplayer());
    LCUtil.logps(ps); 
    int row_player = ps.executeUpdate();
        LOG.debug("deleted player = " + row_player);
        
        
        
        
    
 /*   String msg = "<br/> <h1>Records deleted = " 
                        + " <br/></h1>player = " + player.getIdplayer()
                        + " <br/>score = " + row_score
                        + " <br/>inscription = " + row_inscription
                        + " <br/>handicap = " + row_hcp
                        + " <br/>handicap Index = " + row_hcp_index
                        + " <br/>blocking = " + row_blocking
                        + " <br/>player = " + row_player;
           LOG.debug(msg);
    //    LCUtil.showMessageInfo(msg);
*/
        return true;

}catch (SQLException e){
    String msg = "SQL Exception in " + methodName + e.toString() + ", SQLState = " + e.getSQLState()
            + ", ErrorCode = " + e.getErrorCode();
    LOG.error(msg);
    LCUtil.showMessageFatal(msg);
    return false;
}catch (Exception ex){
    String msg = "Exception in " + methodName + ex;
    LOG.error(msg);
    LCUtil.showMessageFatal(msg);
    return false;
}finally{
        utils.DBConnection.closeQuietly(null, null, null, ps);
}
} //end method
       
 public static void main(String[] args) throws Exception {
     // seule méthode utilisée !! pas accessible via applicatiion
     Connection conn = new DBConnection().getConnection();
  try{
      Player player = new Player();
      player.setIdplayer(111111);
    boolean OK = new DeletePlayer().deletePlayerAndChilds(player, conn);
        LOG.debug("from main, after = " + OK);
 } catch(Exception e) {
            String msg = "Â£Â£ Exception in main = " + e.getMessage();
            LOG.error(msg);
      //      LCUtil.showMessageFatal(msg);
   }finally{
         DBConnection.closeQuietly(conn, null, null , null); 
          }
   } // end main//
} //end class
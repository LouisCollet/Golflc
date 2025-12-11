package delete;

import entite.Club;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import utils.DBConnection;
import utils.LCUtil;

public class DeleteClub implements interfaces.Log, interfaces.GolfInterface{
     private final static String CLASSNAME = utils.LCUtil.getCurrentClassName();
     
  public boolean delete(final Club club, final Connection conn) throws Exception {
    final String methodName = utils.LCUtil.getCurrentMethodName(CLASSNAME);
      PreparedStatement ps = null;
try{ 
    LOG.debug("starting " + methodName);
        LOG.debug(" CASCADING DELETE ATTENTION ! for club "  + club); // new 15-02-2021
        // voir autre methode !!
    final String query = """
        DELETE from club
        WHERE club.idclub = ?
       """ ;
    ps = conn.prepareStatement(query);
    ps.setInt(1, club.getIdclub());
    LCUtil.logps(ps); 
    int row_delete = ps.executeUpdate();
        LOG.debug("deleted Club = " + row_delete);
    String msg = "There are " + row_delete + " Club deleted = " + club;
        LOG.debug(msg);
  //      LCUtil.showMessageInfo(msg);
        return true;
}catch (SQLException e){
    String error = "SQL Exception in DeleteClub = " + e.toString() + ", SQLState = " + e.getSQLState()
            + ", ErrorCode = " + e.getErrorCode();
    LOG.error(error);
    LCUtil.showMessageFatal(error);
    return false;
}catch (Exception ex){
    String msg = "Exception in DeleteClub() " + ex;
    LOG.error(msg);
  //  LCUtil.showMessageFatal(msg);
    return false;
}finally{
        utils.DBConnection.closeQuietly(null, null, null, ps);
}
} //end method

  public boolean deleteClubAndChilds(final Club club,final Connection conn) throws Exception{
       final String methodName = utils.LCUtil.getCurrentMethodName(CLASSNAME);
    PreparedStatement ps = null;
try{  
   // nez fonctionne pas !!
        /* encore à faire : payments-cotisation, greenfee, creditcard, activation
    
     prb si player a un PlayerRole admin (local administrateur)
    SQL Exception in delete.DeletePlayer.deletePlayerAndChilds / java.sql.SQLIntegrityConstraintViolationException:
    Cannot delete or update a parent row: a foreign key constraint fails
    (`golflc`.`club`, CONSTRAINT `club_existe_local_admin` FOREIGN KEY (`ClubLocalAdmin`)
    REFERENCES `player` (`idplayer`)), SQLState = 23000, ErrorCode = 1451
    solution insert value null dans ClubLocalAdmin
    */
     LOG.debug("starting " + methodName);
     LOG.debug("for club = " + club);
     // on commende par le niveau le plus bas !
     
     final String query = """
          DELETE from course
          WHERE course.club_idclub = ?
         """;
    ps = conn.prepareStatement(query); 
  //  ps.setInt(1, club.getIdplayer());
    LCUtil.logps(ps); 
    int row_hcp = ps.executeUpdate();
        LOG.debug("deleted handicap EGA = " + row_hcp);
     
  /*   
     
     
     
  final String query = """
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
            DELETE elete from player
            WHERE player.idplayer = ?
          """;
    ps = conn.prepareStatement(query); 
    ps.setInt(1, player.getIdplayer());
    LCUtil.logps(ps); 
    int row_player = ps.executeUpdate();
        LOG.debug("deleted player = " + row_player);
        
       */ 
        
        
    
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
     //   utils.DBConnection.closeQuietly(null, null, null, ps);
}
} //end method
  
  
 void main() throws SQLException, Exception{
     Connection conn = new DBConnection().getConnection();
 try{
     LOG.debug("entering main with conn = " + conn);
     Club club = new Club();
     club.setIdclub(1122);
     boolean b = new DeleteClub().delete(club, conn);
   // boolean b = new DeleteClub().deleteClubAndChilds(club, conn);
    LOG.debug("from main - resultat deleteclub = " + b);
 } catch (Exception e) {
            String msg = "Â£Â£ Exception in main = " + e.getMessage();
            LOG.error(msg);
      //      LCUtil.showMessageFatal(msg);
   }finally{
     //  DBConnection.closeQuietly(conn, null, null, null); 
          }
} // end method main
} //end class
package delete;

import entite.Round;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import utils.DBConnection;
import utils.LCUtil;

public class DeleteRound implements interfaces.Log, interfaces.GolfInterface{
    public boolean delete(final Round round, final Connection conn) throws Exception {
    PreparedStatement ps = null;
try
{       LOG.debug("starting Delete Round ... = " );
        LOG.debug("Delete round for round "  + round.toString());
    String query = """
       DELETE from round
       WHERE round.idround = ?
     """;
    ps = conn.prepareStatement(query);
    ps.setInt(1, round.getIdround());
    LCUtil.logps(ps); 
    int row_deleted = ps.executeUpdate();
        LOG.debug("deleted Round = " + row_deleted);
    String msg = "<br/> <h1>Round deleted = " + round.getIdround();
        LOG.debug(msg);
        LCUtil.showMessageInfo(msg);
        return true;
}catch (SQLException e){
    String msg = "SQL Exception in DeleteRond for round " + round.getIdround() + " <br/> "
            + e.toString() + ", SQLState = " + e.getSQLState() + ", ErrorCode = " + e.getErrorCode();
    LOG.error(msg);
    LCUtil.showMessageFatal(msg);
    return false;
}catch (Exception ex){
    String err = "Exception in DeleteRound() for round " + round.getIdround() + " <br/> " + ex;
    LOG.error(err);
    LCUtil.showMessageFatal(err);
    return false;
}finally{
        utils.DBConnection.closeQuietly(null, null, null, ps);
}
} //end method
    
    
 public boolean deleteRoundAndChilds(final Round round ,final Connection conn) throws Exception{
    PreparedStatement ps = null;
try{   // ATTENTION
    // utilisé pour deleter les rounds de test
    // supprime TOUTES les inscriptions et scores donc pour tous les joueurs ...
     LOG.debug("starting deleteRoundAndChilds  ... = " );
     LOG.debug("for round = " + round.toString() );
     // on commende par le niveau le plus bas !

  String query = """
          DELETE from score
          WHERE score.player_has_round_round_idround = ?
          """;  
    ps = conn.prepareStatement(query); 
    ps.setInt(1, round.getIdround());
    LCUtil.logps(ps); 
    int row_score = ps.executeUpdate();
        LOG.debug("deleted score = " + row_score);
    
    query = """
        DELETE from player_has_round
        WHERE InscriptionIdRound = ?
     """;
    ps = conn.prepareStatement(query); 
    ps.setInt(1, round.getIdround());
    LCUtil.logps(ps); 
    int row_phr = ps.executeUpdate();
        LOG.debug("deleted inscription = " + row_phr);
    
    /* constraints on handicap_index   
    query = """
          SET FOREIGN_KEY_CHECKS = 0
          DELETE from round
          WHERE round.idround = ?
          SET FOREIGN_KEY_CHECKS = 1
       """ ;
    */
        query = """
          DELETE from round
          WHERE round.idround = ?
    """ ;
    ps = conn.prepareStatement(query);
    ps.setInt(1, round.getIdround());
    LCUtil.logps(ps); 
    int row_rnd = ps.executeUpdate();
        LOG.debug("deleted round with foreign = " + row_rnd);
    // new 29-04-2025    
    query = """ 
          DELETE from payments_greenfee
          WHERE GreenfeeIdRound = ?
    """ ;
    ps = conn.prepareStatement(query);
    ps.setInt(1, round.getIdround());
    LCUtil.logps(ps); 
    int row_pay = ps.executeUpdate();
        LOG.debug("deleted round with foreign = " + row_pay);

    String msg = "<br/> <h1>Records deleted = " 
                        + " <br/></h1>round = " + round.getIdround()
                        + " <br/>score = " + row_score
                        + " <br/>inscription = " + row_phr
                        + " <br/>round = " + row_rnd
                        + " <br/>payment = " + row_pay;
           LOG.debug(msg);
        LCUtil.showMessageInfo(msg);
        return true;

}catch (SQLException e){
    String msg = "SQL Exception in DeleteRoundAndChildds = " + e.toString() + ", SQLState = " + e.getSQLState()
            + ", ErrorCode = " + e.getErrorCode();
    LOG.error(msg);
    LCUtil.showMessageFatal(msg);
    return false;
}catch (Exception ex){
    String msg = "Exception in DeleteRoundAndChilds() " + ex;
    LOG.error(msg);
    LCUtil.showMessageFatal(msg);
    return false;
}finally{
        utils.DBConnection.closeQuietly(null, null, null, ps);
}
} //end method  
 public static void main(String args[])throws SQLException, Exception{      
     Connection conn = new DBConnection().getConnection();
   //  java.util.List<Integer> rounds = new ArrayList<>(java.util.Arrays.asList(587,591,593,594,633));
  //   java.util.List<Integer> rounds = new ArrayList<>(java.util.Arrays.asList(1));
  // faire loop
 try{
    Round round = new Round();
    round.setIdround(760);
    boolean b = new DeleteRound().deleteRoundAndChilds(round, conn);
        LOG.debug("from main - resultat deleteRound = " + b);
 } catch (Exception e) {
            String msg = "Â£Â£ Exception in main = " + e.getMessage();
            LOG.error(msg);
   }finally{
       DBConnection.closeQuietly(conn, null, null, null); 
    }
} // end method main
} //end class
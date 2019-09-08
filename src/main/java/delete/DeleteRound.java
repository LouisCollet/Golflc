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
{       LOG.info("starting Delete Round ... = " );
        LOG.info("Delete round for round "  + round.toString());
    String query = 
       " DELETE from round" +
       " WHERE round.idround = ?" 
        ;
    ps = conn.prepareStatement(query);
    ps.setInt(1, round.getIdround());
    LCUtil.logps(ps); 
    int row_deleted = ps.executeUpdate();
        LOG.info("deleted Round = " + row_deleted);
    String msg = "<br/> <h1>Round deleted = " + round.getIdround();
        LOG.info(msg);
        LCUtil.showMessageInfo(msg);
        return true;
}catch (SQLException e){
    String msg = "SQL Exception in DeleteRond = " + e.toString() + ", SQLState = " + e.getSQLState()
            + ", ErrorCode = " + e.getErrorCode();
    LOG.error(msg);
    LCUtil.showMessageFatal(msg);
    return false;
}catch (Exception ex){
    String msg = "Exception in DeleteRound() " + ex;
    LOG.error(msg);
    LCUtil.showMessageFatal(msg);
    return false;
}finally{
        utils.DBConnection.closeQuietly(null, null, null, ps);
}
} //end method
 public boolean deleteRoundAndChilds(final Round round ,final Connection conn) throws Exception{
    PreparedStatement ps = null;
try{   // ATTENTION
    // utilisé pour deleter les rounds de test
    // supprime TOUTES les inscriptions donc pour tous les joueurs ...
     LOG.info("starting deleteRoundAndChilds  ... = " );
     LOG.info("for round = " + round.toString() );
     // on commende par le niveau le plus bas !
  String query = " delete from score"
               + " where score.player_has_round_round_idround = ?";
    ps = conn.prepareStatement(query); 
    ps.setInt(1, round.getIdround());
    LCUtil.logps(ps); 
    int row_score = ps.executeUpdate();
        LOG.info("deleted score = " + row_score);
    
    query = " delete from player_has_round "
          + " where InscriptionIdRound = ?";
    ps = conn.prepareStatement(query); 
    ps.setInt(1, round.getIdround());
    LCUtil.logps(ps); 
    int row_phr = ps.executeUpdate();
        LOG.info("deleted inscription = " + row_phr);
    
    query = " delete from round"
          + " where round.idround = ?";
    ps = conn.prepareStatement(query); 
    ps.setInt(1, round.getIdround());
    LCUtil.logps(ps); 
    int row_rnd = ps.executeUpdate();
        LOG.info("deleted round = " + row_rnd);

    String msg = "<br/> <h1>Records deleted = " 
                        + " <br/></h1>round = " + round.getIdround()
                        + " <br/>score = " + row_score
                        + " <br/>inscription = " + row_phr
                        + " <br/>round = " + row_rnd;
           LOG.info(msg);
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
 public static void main(String[] args) throws SQLException, Exception{
     Connection conn = new DBConnection().getConnection();
 try{
    Round round = new Round();
    round.setIdround(315);
    boolean b = new DeleteRound().deleteRoundAndChilds(round, conn);
      LOG.info("from main - resultat deleteRound = " + b);
 } catch (Exception e) {
            String msg = "Â£Â£ Exception in main = " + e.getMessage();
            LOG.error(msg);
   }finally{
       DBConnection.closeQuietly(conn, null, null, null); 
    }
} // end method main
} //end class
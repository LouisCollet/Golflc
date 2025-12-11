package find;

import entite.Player;
import entite.Round;
import static interfaces.Log.LOG;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import utils.DBConnection;
import utils.LCUtil;

public class FindCountScore{
private final static String CLASSNAME = utils.LCUtil.getCurrentClassName(); 

  public int find(Connection conn, Player player, Round round, String operation) throws SQLException{
    PreparedStatement ps = null;
    ResultSet rs = null;
    String query = null;
try{
        LOG.debug("entering " + CLASSNAME);
        LOG.debug("round  =  " + round);
 //       LOG.debug("round  =  " + round);
        LOG.debug("operation =  " + operation);

  if(operation.equalsIgnoreCase("rows")) {
      query = """
            SELECT count(*)
            FROM score
            WHERE score.player_has_round_player_idplayer = ?
              AND player_has_round_round_idround = ?
          """ ;
  }else{
       query = """
            SELECT sum(scorestroke)
            FROM score
            WHERE score.player_has_round_player_idplayer = ?
              AND player_has_round_round_idround = ?
         """  ;
  }
    ps = conn.prepareStatement(query);
    ps.setInt(1,player.getIdplayer());
    ps.setInt(2,round.getIdround());
    utils.LCUtil.logps(ps);
    rs = ps.executeQuery();
    if(rs.next()){ 
        // LOG.debug("resultat : getCountScore = " + rs.getInt(1) );
       return rs.getInt(1);
    }else{
      //  LOG.debug("no next : getCountScore = " + rs.getInt(1) );
        return 99;  //error code
    }
} catch(SQLException sqle){
    String msg = "Â£Â£Â£ SQLException in FindCountScore = " + sqle.getMessage() + " ,SQLState = " +
            sqle.getSQLState() + " ,ErrorCode = " + sqle.getErrorCode();
        LOG.error(msg);
    LCUtil.showMessageFatal(msg);
    return 99;
}catch(Exception nfe){
    String msg = "Â£Â£Â£ Exception in FindCountScore = " + nfe.getMessage();
        LOG.error(msg);
    LCUtil.showMessageFatal(msg);
    return 99;
}finally{
      DBConnection.closeQuietly(null, null, rs, ps);
}
} //end method
    
void main() throws Exception , Exception{
    Connection conn = new DBConnection().getConnection();
    Player player = new Player();
    player.setIdplayer(324713);
    Round round = new Round();
    round.setIdround(628);
    round = new read.ReadRound().read(round, conn);
    int i = new FindCountScore().find(conn, player, round,"rows");
      LOG.debug("CountScore = "  + i);
    DBConnection.closeQuietly(conn, null, null, null);
}// end main
} // end class
package find;

import entite.Player;
import entite.Round;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import utils.DBConnection;
import utils.LCUtil;

public class FindInscriptionRound implements interfaces.Log, interfaces.GolfInterface{
    private final static String CLASSNAME = utils.LCUtil.getCurrentClassName();

 public boolean find(final Round round, final Player player, final Connection conn) throws SQLException{   
     final String methodName = utils.LCUtil.getCurrentMethodName(CLASSNAME); 
        LOG.debug("entering : " + methodName); 
        LOG.debug("starting findInscription for rond = " + round);
        LOG.debug("starting findInscription for player = " + player);
    PreparedStatement ps = null;
    ResultSet rs = null;
try{ 
    final String query = """
      SELECT COUNT(*)
      FROM  player_has_round
      WHERE player_has_round.InscriptionIdRound = ?
         AND player_has_round.InscriptionIdPlayer = ?
    """ ;
    ps = conn.prepareStatement(query);
    ps.setInt(1, round.getIdround());
    ps.setInt(2, player.getIdplayer());
    utils.LCUtil.logps(ps);
    rs =  ps.executeQuery();
    int count = 0;
    while(rs.next()){ 
       count = rs.getInt(1);
    }
    
    if(count == 0){
        return false;
    }else{
        return true;
    }
}catch (SQLException e){
    String msg = "SQL Exception in = " + methodName + " / " + e.toString() + ", SQLState = " + e.getSQLState()
            + ", ErrorCode = " + e.getErrorCode();
	LOG.error(msg);
        LCUtil.showMessageFatal(msg);
        return false;
}catch (Exception ex){
    String msg = "Exception in " + methodName + " / "  + ex;
    LOG.error(msg);
    LCUtil.showMessageFatal(msg);
     return false;
}finally{
    DBConnection.closeQuietly(null, null, rs, ps);
}
}//end method

void main() throws SQLException, Exception{
    Connection conn = new DBConnection().getConnection();
    Round round = new Round();
    round.setIdround(633);
 //   round = new load.LoadRound().load(round, conn);
    Player player = new Player();
    player.setIdplayer(324715);
    boolean b = new FindInscriptionRound().find(round, player, conn);
       LOG.debug("inscription found = " + b);
    DBConnection.closeQuietly(conn, null, null, null);
}// end main
} // end Class
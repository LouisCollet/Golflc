package load;

import entite.Player;
import static interfaces.Log.LOG;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import utils.DBConnection;
import utils.LCUtil;

public class LoadPlayer{

public Player load(Player player, Connection conn) throws SQLException{
    PreparedStatement ps = null;
    ResultSet rs = null;
try{
        LOG.info("entering LoadPlayer");
    String pl = utils.DBMeta.listMetaColumnsLoad(conn, "player");
        LOG.info("String from listMetaColumns = " + pl);

final String query = "SELECT "
        + pl
        + " FROM Player"
        + " WHERE idplayer = ?" ;

        LOG.info("Selected Player  = " + player.toString()); 
     ps = conn.prepareStatement(query);
     ps.setInt(1, player.getIdplayer());
     utils.LCUtil.logps(ps); 
     rs =  ps.executeQuery();
     rs.beforeFirst();
     Player c = new Player(); 
     while(rs.next())
        {
               c = entite.Player.mapPlayer(rs);
	}  //end while
    return c;
}catch (SQLException e){
    String msg = "SQLException in LoadPlayer() = " + ", SQLState = " + e.getSQLState()
            + ", ErrorCode = " + e.getErrorCode();
	LOG.error(msg);
        LCUtil.showMessageFatal(msg);
        return null;
}catch (Exception ex){
    LOG.error("Exception ! " + ex);
    LCUtil.showMessageFatal("Exception in LoadPlayer = " + ex.toString() );
     return null;
}
finally
{
       // DBConnection.closeQuietly(conn, null, rs, ps);
    DBConnection.closeQuietly(null, null, rs, ps); // new 14/08/2014
}

} //end method

public static void main(String[] args) throws SQLException, Exception{ // testing purposes
    Connection conn = new DBConnection().getConnection();
    Player player = new Player();
    player.setIdplayer(324713);
    Player p = new LoadPlayer().load(player,conn);
       LOG.info(" club = " + p.toString());
    DBConnection.closeQuietly(conn, null, null, null);

}// end main
} // end class

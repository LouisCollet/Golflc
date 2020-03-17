package load;

import entite.EPlayerPassword;
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
    // ancienne version
    PreparedStatement ps = null;
    ResultSet rs = null;
try{
 //       LOG.info("entering LoadPlayer");
    String pl = utils.DBMeta.listMetaColumnsLoad(conn, "player");
 //       LOG.info("String from listMetaColumns = " + pl);

final String query =
        "SELECT " + pl
        + " FROM Player"
        + " WHERE idplayer = ?" ;
//        LOG.info("Selected Player  = " + player.toString()); 
     ps = conn.prepareStatement(query);
     ps.setInt(1, player.getIdplayer());
     utils.LCUtil.logps(ps); 
     rs =  ps.executeQuery();
     rs.beforeFirst();
     Player p = new Player();
     while(rs.next()){
               p = entite.Player.mapPlayer(rs);
	}  //end while
    return p;
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
}finally{
       // DBConnection.closeQuietly(conn, null, rs, ps);
    DBConnection.closeQuietly(null, null, rs, ps); // new 14/08/2014
}
} //end method


public EPlayerPassword load(final EPlayerPassword epp, final Connection conn) throws SQLException{
    // nouvelle version avec le password !!
    PreparedStatement ps = null;
    ResultSet rs = null;
try{
    LOG.info("entering LoadPlayer with epp = " + epp);
 
    Player player = epp.getPlayer();
    String pl = utils.DBMeta.listMetaColumnsLoad(conn, "player");

    final String query =
        "SELECT " + pl
        + " FROM Player"
        + " WHERE idplayer = ?" ;
//        LOG.info("Selected Player  = " + player.toString()); 
     ps = conn.prepareStatement(query);
     ps.setInt(1, player.getIdplayer());
     utils.LCUtil.logps(ps); 
     rs =  ps.executeQuery();
     rs.beforeFirst();
     EPlayerPassword e = new EPlayerPassword();
     while(rs.next()){
          e.setPlayer(entite.Player.mapPlayer(rs));
          e.setPassword(entite.Password.mapPassword(rs));
	}  //end while
    return e;
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
}finally{
       // DBConnection.closeQuietly(conn, null, rs, ps);
    DBConnection.closeQuietly(null, null, rs, ps); // new 14/08/2014
}
} //end metho

public static void main(String[] args) throws SQLException, Exception{ // testing purposes
    Connection conn = new DBConnection().getConnection();
    Player player = new Player();
    player.setIdplayer(324713);
    Player p = new LoadPlayer().load(player,conn);
       LOG.info(" main : player loaded = " + p.toString());
    DBConnection.closeQuietly(conn, null, null, null);
}// end main
} // end class
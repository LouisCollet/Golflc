package read;

import entite.composite.EPlayerPassword;
import entite.Player;
import static interfaces.Log.LOG;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import utils.DBConnection;
import utils.LCUtil;

public class ReadPlayer{
   private final static String CLASSNAME = utils.LCUtil.getCurrentClassName();
public Player read(Player player, Connection conn) throws SQLException{
    // ancienne version
    PreparedStatement ps = null;
    ResultSet rs = null;
    final String methodName = utils.LCUtil.getCurrentMethodName(CLASSNAME);
try{
  //    LOG.debug("entering ReadPlayer.read with player = " + player);
 //   String pl = utils.DBMeta.listMetaColumnsLoad(conn, "player");
 //       LOG.debug("String from listMetaColumns = " + pl);

final String query = """
        SELECT *
        FROM Player
        WHERE idplayer = ?
       """ ;
//        LOG.debug("Selected Player  = " + player.toString()); 
     ps = conn.prepareStatement(query);
     ps.setInt(1, player.getIdplayer());
     utils.LCUtil.logps(ps); 
     rs =  ps.executeQuery();
     Player p = new Player();
     int i = 0;
     while(rs.next()){
           i++;
           p = entite.Player.map(rs);
	}  //end while
 //       LOG.debug("player mapped = " + p);
   //  if(p.getIdplayer() == null){
      if(i == 0){
         String msg = "££ Empty Result in " + methodName;
         LOG.error(msg);
         LCUtil.showMessageFatal(msg);
         return null;
     }else{
         LOG.debug("ResultSet " + methodName + " has " + i + " lines.");
     }
    return p;
}catch (SQLException e){
    String msg = "SQLException in LoadPlayer() = " + ", SQLState = " + e.getSQLState()
            + ", ErrorCode = " + e.getErrorCode();
	LOG.error(msg);
        LCUtil.showMessageFatal(msg);
    //    return null;
        return player; // mod 29-05-2023
}catch (Exception ex){
    LOG.error("Exception ! " + ex);
    LCUtil.showMessageFatal("Exception in LoadPlayer = " + ex.toString() );
     return null;
}finally{
       // DBConnection.closeQuietly(conn, null, rs, ps);
    DBConnection.closeQuietly(null, null, rs, ps); // new 14/08/2014
}
} //end method


public EPlayerPassword read(EPlayerPassword epp, final Connection conn) throws SQLException{
    // nouvelle version avec le password !!
    final String methodName = utils.LCUtil.getCurrentMethodName(CLASSNAME);
    PreparedStatement ps = null;
    ResultSet rs = null;
try{
    LOG.debug("entering " + methodName + " with epp = " + epp);
//    Player player = epp.getPlayer();
    String pl = utils.DBMeta.listMetaColumnsLoad(conn, "player");

 //   mod 27-02-2024 final String query =
 //       "SELECT " + pl
 //       + " FROM Player"
 //       + " WHERE idplayer = ?" ;
//        LOG.debug("Selected Player  = " + player.toString()); 

final String query = """
        SELECT *
        FROM Player
        WHERE idplayer = ?
       """ ;
     ps = conn.prepareStatement(query);
     ps.setInt(1, epp.getPlayer().getIdplayer());
     utils.LCUtil.logps(ps); 
     rs =  ps.executeQuery();
  //    EPlayerPassword epp2 = new EPlayerPassword();
     int i = 0;
     while(rs.next()){
          i++;
          epp.setPlayer(entite.Player.map(rs));
          epp.setPassword(entite.Password.map(rs));
	}  //end while
     LOG.debug("ResultSet " + methodName + " has " + i + " lines.");
    return epp;
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

void main() throws SQLException, Exception{ // testing purposes
    Connection conn = new DBConnection().getConnection();
    Player player = new Player();
    player.setIdplayer(324715);
 //   Player p = new ReadPlayer().load(player,conn);
  //     LOG.debug(" main : player loaded = " + p.toString());
    DBConnection.closeQuietly(conn, null, null, null);
}// end main
} // end class
package read;

import entite.Blocking;
import entite.Player;
import static interfaces.Log.LOG;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import utils.DBConnection;

public class LoadBlocking{

private final static String CLASSNAME = utils.LCUtil.getCurrentClassName();

public Blocking load(Player player, Connection conn) throws SQLException, Exception{
    PreparedStatement ps = null;
    ResultSet rs = null;
    final String methodName = utils.LCUtil.getCurrentMethodName(CLASSNAME);
try{
//        LOG.debug("entering  "+ methodName + " for player = " + player.getIdplayer());
     String query =
              "SELECT *"
              + " from blocking"
              + " where BlockingPlayerId = ?"
             ;
      ps = conn.prepareStatement(query);
      ps.setInt(1, player.getIdplayer());
        utils.LCUtil.logps(ps); 
      rs = ps.executeQuery();
      int i = 0;      
        Blocking a = null;
	while(rs.next()){
            i++;
             a = entite.Blocking.mapBlocking(rs);
	}
//        LOG.debug("i = " + i);
       if(i == 0){
         String msg = "££ Empty Result Table in " + methodName;
         LOG.error(msg);
  //       LCUtil.showMessageInfo(msg);
         return null;
     }else{
         LOG.debug("ResultSet " + methodName + " has " + i + " lines.");
     }  
   return a;
}catch(Exception ex){
    String err = "-- Exception in ! " + CLASSNAME + " / " + ex.toString() + "/";
    LOG.error(err);
    return null;
} // end catch
finally{
    DBConnection.closeQuietly(null, null, rs, ps);
}
} // end method find
    
 void main() throws Exception , Exception{
    Connection conn = new DBConnection().getConnection();
    Player player = new Player();
    player.setIdplayer(206658);
    Blocking blocking = new LoadBlocking().load(player, conn);
        LOG.debug("Blocking found = " + blocking);
    DBConnection.closeQuietly(conn, null, null, null);
}// end main
 
} // end class
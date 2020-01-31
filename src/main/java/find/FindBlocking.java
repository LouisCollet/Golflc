package find;

import entite.Blocking;
import entite.Player;
import static interfaces.Log.LOG;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import utils.DBConnection;

public class FindBlocking{

final private static String CLASSNAME = Thread.currentThread().getStackTrace()[1].getClassName(); 

public Blocking find(Player player, Connection conn) throws SQLException, Exception{
    PreparedStatement ps = null;
    ResultSet rs = null;
try{
        LOG.info("entering FindBlocking for player = " + player.getIdplayer());
        LOG.info("connection = " + conn);
     String bl = utils.DBMeta.listMetaColumnsLoad(conn, "blocking");
     String query =
              "SELECT " + bl +
              " from blocking"
              + " where BlockingPlayerId = ?"
             ;
      ps = conn.prepareStatement(query);
      ps.setInt(1, player.getIdplayer());
        utils.LCUtil.logps(ps); 
      rs = ps.executeQuery();
      rs.last(); //on récupère le numéro de la ligne
            LOG.info("ResultSet FindBlocking has " + rs.getRow() + " lines.");
        if(rs.getRow() > 1)
            { throw new Exception(" -- More than one Blocking = " + rs.getRow() );  }
        rs.beforeFirst(); //on replace le curseur avant la première ligne
          //LOG.info("just before while ! ");
        Blocking a = null;
	while(rs.next()){
             a = entite.Blocking.mapBlocking(rs);
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
    
 public static void main(String[] args) throws Exception , Exception{
    Connection conn = new DBConnection().getConnection();
    Player player = new Player();
    player.setIdplayer(206658);
    Blocking blocking = new FindBlocking().find(player, conn);
        LOG.info("Blocking found = " + blocking);
    DBConnection.closeQuietly(conn, null, null, null);
}// end main
 
} // end class
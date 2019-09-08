package find;

import entite.Player;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import utils.DBConnection;
import utils.LCUtil;

public class FindPlayer implements interfaces.Log, interfaces.GolfInterface{
    final private static String CLASSNAME = Thread.currentThread().getStackTrace()[1].getClassName(); 

//public Player findPlayer(final int in_idplayer, final Connection conn) throws SQLException{
 public Player findPlayer(final Player player, final Connection conn) throws SQLException{   
    String CLASSNAME2 = Thread.currentThread().getStackTrace()[1].getClassName(); 
        LOG.info("entering : " + CLASSNAME2); 
        LOG.info("starting findPlayer for player = " + player);
    PreparedStatement ps = null;
    ResultSet rs = null;
try{ 
    String p= utils.DBMeta.listMetaColumnsLoad(conn, "player");
    String query = 
    "SELECT " + p +
"    from Player " +
"    where Player.idplayer = ?;"
     ;
    ps = conn.prepareStatement(query);
 //   ps.setInt(1, in_idplayer);
    ps.setInt(1, player.getIdplayer());
        utils.LCUtil.logps(ps);
    rs =  ps.executeQuery();
        rs.last(); //on récupère le numéro de la ligne
            LOG.info("ResultSet FindPlayer has " + rs.getRow() + " lines.");
        if(rs.getRow() > 1){
            throw new Exception(" -- More than 1 player = " + rs.getRow() );  }
        rs.beforeFirst(); //on replace le curseur avant la première ligne
          //LOG.info("just before while ! ");
        Player pl = null; // = 0.0;
	while(rs.next()){
             pl = entite.Player.mapPlayer(rs);
	}
        return pl;
}catch (SQLException e){
    String msg = "SQL Exception = " + e.toString() + ", SQLState = " + e.getSQLState()
            + ", ErrorCode = " + e.getErrorCode();
	LOG.error(msg);
        LCUtil.showMessageFatal(msg);
        return null;
}catch (Exception ex){
    String msg = "Exception in " + CLASSNAME2 + " / "  + ex;
    LOG.error(msg);
    LCUtil.showMessageFatal(msg);
     return null;
}finally{
    DBConnection.closeQuietly(null, null, rs, ps); // new 14/08/2014
}
}//end method

public static void main(String[] args) throws SQLException, Exception{ // testing purposes
    Connection conn = new DBConnection().getConnection();
    Player player = new Player();
    player.setIdplayer(324713);
    Player p1 = new FindPlayer().findPlayer(player, conn);
       LOG.info("player found = " + p1.toString());
    DBConnection.closeQuietly(conn, null, null, null);
}// end main
} // end Class
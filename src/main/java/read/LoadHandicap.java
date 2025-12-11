package read;

import entite.Handicap;
import entite.Player;
import entite.Round;
import static interfaces.Log.LOG;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.Month;
import utils.DBConnection;
import utils.LCUtil;

public class LoadHandicap{

public Handicap load(Player player, Round round, Connection conn) throws SQLException{
    PreparedStatement ps = null;
    ResultSet rs = null;
try{
   //     LOG.debug("entering LoadHandicap");
       LOG.debug("player =" + player.toString());
    String ha = utils.DBMeta.listMetaColumnsLoad(conn, "handicap");
   final String query = 
    "SELECT " + ha +
"   from handicap " +
"   where handicap.player_idplayer=?" +
"   and date(?)" +
"	between handicap.idhandicap" +
"	and handicap.handicapend"
        ;

     ps = conn.prepareStatement(query);
     ps.setInt(1, player.getIdplayer());
 //    java.sql.Timestamp ts = Timestamp.valueOf(round.getRoundDate());
     ps.setTimestamp(2,Timestamp.valueOf(round.getRoundDate()));
     
     LCUtil.logps(ps); 
     rs =  ps.executeQuery();
 //    rs.beforeFirst();
     Handicap h = new Handicap(); 
     while(rs.next()){
                   h = entite.Handicap.map(rs);
	}  //end while
    return h;
}catch (SQLException e){
    String msg = "SQLException in LoadHandicap() = " + ", SQLState = " + e.getSQLState()
            + ", ErrorCode = " + e.getErrorCode();
	LOG.error(msg);
        LCUtil.showMessageFatal(msg);
        return null;
}catch (Exception ex){
    LOG.error("Exception ! " + ex);
  //  LCUtil.showMessageFatal("Exception in LoadClub = " + ex.toString() );
     return null;
}finally{
    DBConnection.closeQuietly(null, null, rs, ps); // new 14/08/2014
}
} //end method

void main() throws SQLException, Exception{
    DBConnection dbc = new DBConnection();
    Connection conn = dbc.getConnection();
    Player player = new Player();
    player.setIdplayer(324713);
    LOG.debug("line 010");
    Round round = new Round();
     LOG.debug("line 011");
    LocalDateTime ldt = LocalDateTime.of(2017,Month.AUGUST,26,0,0);
    round.setRoundDate(ldt);
        LOG.debug("line 012");
    LoadHandicap lh = new LoadHandicap();
    Handicap h = lh.load(player, round , conn);
    LOG.debug(" handicap = " + h.toString());
//for (int x: par )
//        LOG.debug(x + ",");
DBConnection.closeQuietly(conn, null, null, null);

}// end main
} // end class

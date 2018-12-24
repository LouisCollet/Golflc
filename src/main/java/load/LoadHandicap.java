package load;

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

public class LoadHandicap
{

public Handicap LoadHandicap(Player player, Round round, Connection conn) throws SQLException
{
    PreparedStatement ps = null;
    ResultSet rs = null;
try{
   //     LOG.info("entering LoadHandicap");
       LOG.info("player =" + player.toString());
    String ha = utils.DBMeta.listMetaColumnsLoad(conn, "handicap");
       final String query = "SELECT "
               + ha +
             //  + "idhandicap, handicapend, handicapplayer" +
"   from handicap " +
"   where handicap.player_idplayer=?" +
"   and date(?)" +
"	between handicap.idhandicap" +
"	and handicap.handicapend"
        ;

     ps = conn.prepareStatement(query);
     ps.setInt(1, player.getIdplayer());
     java.sql.Timestamp ts = Timestamp.valueOf(round.getRoundDate());
     ps.setTimestamp(2,ts);
     
     LCUtil.logps(ps); 
     rs =  ps.executeQuery();
     rs.beforeFirst();
     Handicap h = new Handicap(); 
     while(rs.next())
                {
                   h = entite.Handicap.mapHandicap(rs);
	//	h.setHandicapStart(rs.getDate("idhandicap"));
        //        h.setHandicapEnd(rs.getDate("HandicapEnd") );
         //       h.setHandicapPlayer(rs.getBigDecimal("handicapPlayer"));
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
}
finally
{
    DBConnection.closeQuietly(null, null, rs, ps); // new 14/08/2014
}
} //end method

public static void main(String[] args) throws SQLException, Exception // testing purposes
{
    DBConnection dbc = new DBConnection();
    Connection conn = dbc.getConnection();
    Player player = new Player();
    player.setIdplayer(324713);
    LOG.info("line 010");
    Round round = new Round();
     LOG.info("line 011");
    LocalDateTime ldt = LocalDateTime.of(2017,Month.AUGUST,26,0,0);
    round.setRoundDate(ldt);
        LOG.info("line 012");
    LoadHandicap lh = new LoadHandicap();
    Handicap h = lh.LoadHandicap(player, round , conn);
    LOG.info(" handicap = " + h.toString());
//for (int x: par )
//        LOG.info(x + ",");
DBConnection.closeQuietly(conn, null, null, null);

}// end main
} // end class

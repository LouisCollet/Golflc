package find;

import entite.Player;
import entite.Round;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import utils.DBConnection;
import utils.LCUtil;

public class FindHandicap implements interfaces.Log, interfaces.GolfInterface
{
    final private static String ClassName = Thread.currentThread().getStackTrace()[1].getClassName(); 
    
public double findPlayerHandicap(final Player player, final Round round, final Connection conn) throws SQLException
{
    LOG.info("entering findPlayerHandicap);");
    LOG.info("starting findPlayerHandicap for player = " + player.getIdplayer());
    LOG.info("starting findPlayerHandicap for game = " + round.toString());
////if(liste == null)
////{
    PreparedStatement ps = null;
    ResultSet rs = null;
try
{
  String query = 
    "SELECT handicap.handicapPlayer" +
"    from handicap " +
"    where" +
"        handicap.player_idplayer = ?" +
"        and date(?)" +
          //and date_add('2017-08-26 09:34:10', interval 1 day)
//"           and date_add(?, interval 1 day)" +  // mod 27/082017
"            between handicap.idhandicap and handicap.handicapend;"
     ;
//        LOG.info("player = " + player) ;
    ps = conn.prepareStatement(query);
    ps.setInt(1, player.getIdplayer() );
  //  ps.setDate(2, LCUtil.getSqlDate(round.getRoundDate()));
  //  java.util.Date d = java.sql.Date.valueOf(round.getRoundDate());
    java.sql.Timestamp ts = Timestamp.valueOf(round.getRoundDate());
  //  ps.setDate(2, LCUtil.getSqlDate(d));

    ps.setTimestamp(2,ts);
        
        utils.LCUtil.logps(ps);
    rs =  ps.executeQuery();
        rs.last(); //on récupère le numéro de la ligne
            LOG.info("ResultSet FindHandicap has " + rs.getRow() + " lines.");
        if(rs.getRow() > 1)
            {   throw new Exception(" -- More than 1 handicap = " + rs.getRow() );  }
        rs.beforeFirst(); //on replace le curseur avant la première ligne
  ////      liste = new ArrayList<>();
          //LOG.info("just before while ! ");
        BigDecimal t = null; // = 0.0;
	while(rs.next())
        {
             t = rs.getBigDecimal("handicapPlayer");
	}
         // Print the name from the list....
 ////       for(PlayerHasRound model : liste) {
////            LOG.info("TeePlayer = "  + model.getInscriptionTeeStart() );}
 ////       String t = liste.get(0).getInscriptionTeeStart();
        LOG.info("HandicapPlayer = "  + t);
  //  return liste;
        return t.doubleValue();
}catch (SQLException e){
    String msg = "SQL Exception = " + e.toString() + ", SQLState = " + e.getSQLState()
            + ", ErrorCode = " + e.getErrorCode();
	LOG.error(msg);
        LCUtil.showMessageFatal(msg);
        return 0;
}catch (NullPointerException npe){   
    String msg = "NullPointerException in FindHandicapPlayer()" + npe;
    LOG.error(msg);
    LCUtil.showMessageFatal(msg);
     return 0;
}catch (Exception ex){
    String msg = "Exception in FindHandicapPlayer()" + ex;
    LOG.error(msg);
    LCUtil.showMessageFatal(msg);
     return 0;
}
finally
{
  //  DBConnection.closeQuietly(conn, null, rs, ps);
    DBConnection.closeQuietly(null, null, rs, ps); // new 14/08/2014
}

}//end method

public static void main(String[] args) throws SQLException, Exception // testing purposes
{
  //  LOG.info("Input main = " + s);
    DBConnection dbc = new DBConnection();
Connection conn = dbc.getConnection();
    Player player = new Player();
    Round round =new Round(); 
player.setIdplayer(324713);
round.setIdround(260);
FindHandicap fh = new FindHandicap ();
  //  String str = pc.checkPassword(uuid, conn);
fh.findPlayerHandicap(player,round, conn);
//for (int x: par )
//        LOG.info(x + ",");
DBConnection.closeQuietly(conn, null, null, null);

}// end main
    
} // end Class


package find;

import entite.Player;
import entite.Round;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import utils.DBConnection;
import utils.LCUtil;

public class FindHandicap implements interfaces.Log, interfaces.GolfInterface{
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
try{
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

        LOG.info("HandicapPlayer = "  + t);
  //  return liste;
        return t.doubleValue();
}catch (SQLException e){
    String msg = "SQL Exception = " + e.toString() + ", SQLState = " + e.getSQLState()
            + ", ErrorCode = " + e.getErrorCode();
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
 //   DBConnection dbc = new DBConnection();
    Connection conn = new DBConnection().getConnection();
    Player player = new Player();
    player.setIdplayer(324713);
    Round round = new Round(); // round 431
    
    LocalDate d = LocalDate.of(2019, 3, 23);
    LocalTime t = LocalTime.of(9, 57, 0, 0);
  //  LocalDateTime dt = LocalDateTime.of(d, t);
 //       System.out.println(dt);
    // alternative pour s'amuser !!
  //  round.setRoundDate(LocalDateTime.of(2019, Month.MARCH, 23, 9,57));
    round.setRoundDate(LocalDateTime.of(d,t));
    double dd = new FindHandicap ().findPlayerHandicap(player,round, conn);
        LOG.info("handicap = " + dd);
    DBConnection.closeQuietly(conn, null, null, null);

}// end main
    
} // end Class


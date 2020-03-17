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
    
public double find(final Player player, final Round round, final Connection conn) throws SQLException{
    LOG.info("entering findPlayerHandicap);");
    LOG.info("starting findPlayerHandicap for player = " + player.toString());
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
"            between handicap.idhandicap and handicap.handicapend;"
     ;
//        LOG.info("player = " + player) ;
    ps = conn.prepareStatement(query);
    ps.setInt(1, player.getIdplayer() );
 //   java.sql.Timestamp ts = Timestamp.valueOf(round.getRoundDate());
    ps.setTimestamp(2,Timestamp.valueOf(round.getRoundDate()));
        utils.LCUtil.logps(ps);
    rs =  ps.executeQuery();
        rs.last(); //on récupère le numéro de la ligne
            LOG.info("ResultSet FindHandicap has " + rs.getRow() + " lines.");
        if(rs.getRow() > 1){
            String msg =" -- More than 1 handicap = " + rs.getRow();
            LOG.info(msg);
            throw new Exception(msg);
        }
        rs.beforeFirst(); //on replace le curseur avant la première ligne
  ////      liste = new ArrayList<>();
          //LOG.info("just before while ! ");
        BigDecimal t = null; // = 0.0;
	while(rs.next()){
             t = rs.getBigDecimal("handicapPlayer");
	}
        LOG.info("HandicapPlayer = "  + t);
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
    Round round = new Round(); // round 438
    LocalDate d = LocalDate.of(2019, 4, 6);
    LocalTime t = LocalTime.of(9, 57, 0, 0);
  //  LocalDateTime dt = LocalDateTime.of(d, t);
 //       System.out.println(dt);
    // alternative pour s'amuser !!
  //  round.setRoundDate(LocalDateTime.of(2019, Month.MARCH, 23, 9,57));
    round.setRoundDate(LocalDateTime.of(d,t));
    double dd = new FindHandicap ().find(player,round, conn);
        LOG.info("handicap = " + dd);
    DBConnection.closeQuietly(conn, null, null, null);

}// end main
    
} // end Class


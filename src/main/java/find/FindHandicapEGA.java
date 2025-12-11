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

public class FindHandicapEGA implements interfaces.Log, interfaces.GolfInterface{
    private final static String CLASSNAME = utils.LCUtil.getCurrentClassName();
    
public double find(final Player player, final Round round, final Connection conn) throws SQLException{
   final String methodName = utils.LCUtil.getCurrentMethodName(CLASSNAME);
     LOG.debug("entering " + methodName);
     LOG.debug(" for player = " + player);
     LOG.debug("Round Date = " + round.getRoundDate());
     LOG.debug(" for round = " + round);

    PreparedStatement ps = null;
    ResultSet rs = null;
try{
  final String query = 
    "SELECT handicap.handicapPlayerEGA" +
"    FROM handicap " +
"    WHERE" +
"        handicap.player_idplayer = ?" +
"        and date(?)" +
"            between handicap.idhandicap and handicap.handicapend;"
     ;
//        LOG.debug("player = " + player) ;
    ps = conn.prepareStatement(query);
    ps.setInt(1, player.getIdplayer() );
 //   java.sql.Timestamp ts = Timestamp.valueOf(round.getRoundDate());
    ps.setTimestamp(2,Timestamp.valueOf(round.getRoundDate()));
        utils.LCUtil.logps(ps);
    rs =  ps.executeQuery();
    BigDecimal t = BigDecimal.ZERO;
    int i = 0;
	while(rs.next()){
            i++;
             t = rs.getBigDecimal("handicapPlayerEGA");
	}
        // tester s'il y en a plusieurs 
     LOG.debug("nombre de handicapEGA = "  + i);
        LOG.debug("HandicapPlayerEGA = "  + t);
        return t.doubleValue();
}catch (SQLException e){
    String msg = "SQL Exception in = " + methodName + e.toString() + ", SQLState = " + e.getSQLState()
            + ", ErrorCode = " + e.getErrorCode();
	LOG.error(msg);
        LCUtil.showMessageFatal(msg);
        return 0;
}catch (Exception ex){
    String msg = "Exception in " + methodName + ex;
    LOG.error(msg);
    LCUtil.showMessageFatal(msg);
     return 0;
}finally{
  //  DBConnection.closeQuietly(conn, null, rs, ps);
    DBConnection.closeQuietly(null, null, rs, ps); // new 14/08/2014
}
}//end method

void main() throws SQLException, Exception // testing purposes
{
  //  LOG.debug("Input main = " + s);
    Connection conn = new DBConnection().getConnection();
       LOG.debug("number of rows of table handicap = " + utils.LCUtil.CountRows(conn, "handicap"));
    Player player = new Player();
    player.setIdplayer(324713);
    Round round = new Round(); 
    round.setIdround(518);
    round = new read.ReadRound().read(round, conn);
//    LocalDate d = LocalDate.of(2019, Month.APRIL, 6);
    LocalTime t = LocalTime.of(12, 59, 59, 0);
    LocalDate d = LocalDateTime.now().toLocalDate();
    LocalDateTime ldt = LocalDateTime.of(d, t);
    
 //   round.setRoundDate(LocalDateTime.of(d,t));
    double dd = new FindHandicapEGA ().find(player,round, conn);
        LOG.debug("from main - handicapPlayerEGA = " + dd);
    DBConnection.closeQuietly(conn, null, null, null);
}// end main
} // end Class
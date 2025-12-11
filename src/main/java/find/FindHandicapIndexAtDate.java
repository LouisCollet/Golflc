package find;

import entite.HandicapIndex;
import entite.Round;
import static interfaces.Log.LOG;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import utils.DBConnection;
import utils.LCUtil;

public class FindHandicapIndexAtDate {
    private final static String CLASSNAME = utils.LCUtil.getCurrentClassName();

public HandicapIndex find(HandicapIndex handicapIndex, final Connection conn) throws SQLException{    
    final String methodName = utils.LCUtil.getCurrentMethodName(CLASSNAME); 
        LOG.debug("entering : " + methodName);
  //      LOG.debug("starting " + methodName + " for HandicapIndex = " + handicapIndex); //.getHandicapPlayerId());
    PreparedStatement ps = null;
    ResultSet rs = null;
try{
   final String query = """
         SELECT *
         FROM handicap_index
         WHERE HandicapPlayerId = ?
         AND HandicapDate < ?
         ORDER BY HandicapDate DESC
         LIMIT 1
   """;

//  hcp à une date déterminée
    ps = conn.prepareStatement(query);
    ps.setInt(1, handicapIndex.getHandicapPlayerId());
    ps.setTimestamp(2,Timestamp.valueOf(handicapIndex.getHandicapDate()));
    utils.LCUtil.logps(ps);
    rs = ps.executeQuery();
//    HandicapIndex handicapIndex = new HandicapIndex();
    int i = 0;
    if(rs.next()){
        i++;
        handicapIndex = entite.HandicapIndex.map(rs);
     }
    if(i == 0){
         String msg = "££ No HandicapIndex found !! " + methodName;
         LOG.error(msg);
         LCUtil.showMessageFatal(msg);
         // pour le passage de EGA à WHS
         /*/ enlevé 23-09-2024 
         handicapIndex.setHandicapPlayerId(handicapIndex.getHandicapPlayerId());
         handicapIndex.setHandicapDate(handicapIndex.getHandicapDate().minusMonths(1));  // fictif
         handicapIndex.setHandicapRoundId(handicapIndex.getHandicapRoundId());
         handicapIndex.setHandicapPlayedStrokes((short) 0);
         handicapIndex.setHandicapWHS(new BigDecimal(27.9)); // fictif !!
*/
         var hi = new create.CreateHandicapIndex().create(handicapIndex, conn);
         msg = "compléter manuellement la situation de départ // created = " + hi;
         LOG.info(msg);
         LCUtil.showMessageInfo(msg);
         return null;
     }else{
         LOG.debug(" Handicap Index found = " + handicapIndex.getHandicapWHS()); //  + " at the date " + handicapIndex.getHandicapDate());
 //        LOG.debug("ResultSet " + methodName + " has " + liste.size() + " lines.");
     }
 //       LOG.debug("i = " + i + " Handicap Index = " + handicapIndex + " at the date " + handicapIndex.getHandicapDate());
     return handicapIndex;
}catch (SQLException e){
    String msg = "SQL Exception in = " + methodName + " /" + e.toString() + ", SQLState = " + e.getSQLState()
            + ", ErrorCode = " + e.getErrorCode();
	LOG.error(msg);
        LCUtil.showMessageFatal(msg);
        return null;
}catch (Exception ex){
    String msg = "Exception in " + methodName + " / "  + ex;
    LOG.error(msg);
    LCUtil.showMessageFatal(msg);
     return null;
}finally{
    DBConnection.closeQuietly(null, null, rs, ps); // new 14/08/2014
}
// return false;
}//end method

void main() throws SQLException, Exception{ // testing purposes
  final Connection conn = new DBConnection().getConnection();
  HandicapIndex handicapIndex = new HandicapIndex();
  handicapIndex.setHandicapPlayerId(324713);
  Round round = new Round();
  round.setIdround(590);
  round = new read.ReadRound().read(round, conn);
  handicapIndex.setHandicapDate(round.getRoundDate());
  HandicapIndex hi = new find.FindHandicapIndexAtDate().find(handicapIndex, conn);
        LOG.debug("FindHandicapIndexAtDate  = " + hi.getHandicapWHS());
  DBConnection.closeQuietly(conn, null, null, null);
}// end main

} // end Class
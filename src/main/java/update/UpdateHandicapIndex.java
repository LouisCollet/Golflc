package update;

import entite.HandicapIndex;
import static interfaces.Log.LOG;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import utils.DBConnection;
import utils.LCUtil;

public class UpdateHandicapIndex {
     private final static String CLASSNAME = utils.LCUtil.getCurrentClassName();
     
 public boolean update(final HandicapIndex handicapIndex, final Connection conn) throws SQLException{
  final String methodName = utils.LCUtil.getCurrentMethodName(CLASSNAME);
    PreparedStatement ps = null;
try{
        LOG.debug("...starting " + methodName);
        LOG.debug("...for handicapIndex " + handicapIndex);
 //       LOG.debug("...for playerId " + handicapIndex.getHandicapPlayerId());
    final String query = """
            UPDATE handicap_index
             SET HandicapWHS = ?,
                HandicapComment = ?,
                HandicapSoftHardCap = ?,
                HandicapExceptionalScoreReduction = ?,
                HandicapPreviousLowHandicap = ?
             WHERE HandicapId = ?
            """ ;
    ps = conn.prepareStatement(query);
    ps.setBigDecimal(1, handicapIndex.getHandicapWHS());
    // pourrait aussi être EDS : d'ou vient l'info ??
    ps.setString(2, "UPD-" + handicapIndex.getHandicapComment()); // mod 17-08-2021
    ps.setString(3, handicapIndex.getHandicapSoftHardCap());
    ps.setShort(4, handicapIndex.getHandicapExceptionalScoreReduction());
    ps.setDouble(5, handicapIndex.getLowHandicapIndex());
 // key WHERE field
    ps.setInt(6,handicapIndex.getHandicapId());

    utils.LCUtil.logps(ps);
    int row = ps.executeUpdate();
    if(row != 0){
          String msg = "successful UPDATE HandicapIndex = " + handicapIndex;
          LOG.info(msg);
          LCUtil.showMessageInfo(msg);
          return true;
     }else{
          String msg = "-- UNsuccessful result in " + methodName + " for player : " + handicapIndex.getHandicapPlayerId();
          LOG.error(msg);
          LCUtil.showMessageFatal(msg);
          return false;
        }
}catch(SQLException e){
    String msg = "SQL Exception in " + methodName + e.toString() + ", SQLState = " + e.getSQLState()
            + ", ErrorCode = " + e.getErrorCode();
	LOG.error(msg);
        LCUtil.showMessageFatal(msg);
        return false;
}catch(Exception ex){
    String msg = "Exception in " + methodName + ex;
    LOG.error(msg);
    LCUtil.showMessageFatal(msg);
    return false;
}finally{
        DBConnection.closeQuietly(null, null, null, ps);
}
} //end method
 
 void main() throws Exception, SQLException{
    Connection conn = new DBConnection().getConnection();
     HandicapIndex index = new HandicapIndex();
 /*
    Player player = new Player();
    player.setIdplayer(324713);
    player = new load.LoadPlayer().load(player, conn);
    Round round = new Round();
    round.setIdround(487);
   
   index.setHandicapScoreDifferential(new BigDecimal("28.6"));
   index.setHandicapPlayerId(player.getIdplayer());
   index.setHandicapRoundId(round.getIdround());
   index.setHandicapDate(round.getRoundDate());
 */
    index.setHandicapId(26); // changer si modification !!
    index.setHandicapWHS(BigDecimal.valueOf(2.3));
    index.setHandicapComment("no comment for this handicap");
    Short s1 = -3;
    index.setHandicapExceptionalScoreReduction(s1);
    index.setHandicapSoftHardCap("capM"); // 4 pos
    LOG.debug("still in main - index = " + index);
    boolean b = new update.UpdateHandicapIndex().update(index, conn);
 //   round.setRoundDate(LocalDateTime.of(2019,Month.APRIL,01,0,0));
 //   round.setRoundQualifying("C");  // "C" = counting, N = non qualifying et Y = qualifying
     LOG.debug(" Voici le résultat : = " + b);
     DBConnection.closeQuietly(conn, null, null, null); 
}// end main
 
 
 
} //end class
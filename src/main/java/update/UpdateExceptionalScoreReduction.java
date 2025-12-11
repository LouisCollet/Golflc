package update;

import entite.Player;
import entite.Round;
import static interfaces.Log.LOG;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import utils.DBConnection;
import utils.LCUtil;

public class UpdateExceptionalScoreReduction{
    private final static String CLASSNAME = utils.LCUtil.getCurrentClassName();
    
public boolean update(final Player player, double esr, final Connection conn) throws SQLException {
   final String methodName = utils.LCUtil.getCurrentMethodName(CLASSNAME);
   PreparedStatement ps = null;
  try {
       LOG.debug("entering " + methodName);
       LOG.debug(" for player = " + player);
 //      LOG.debug(" for round = " + round);
       LOG.debug(" for ExceptionalScoreReduction = " + esr);
/*
   final String query = 
       "UPDATE handicap_index AS UPD," +
       " (SELECT * FROM handicap_index" +
       "   WHERE HandicapPlayerid = ?" +
       "   ORDER BY HandicapDate desc" +
       "   LIMIT 20) SEL" +
       "   SET UPD.HandicapExceptionalScoreReduction = ?," +
       "   UPD.HandicapScoreDifferential = UPD.HandicapScoreDifferential ?" +
       "   WHERE UPD.HandicapId = SEL.HandicapId;"
           ;
   */
// mod 16/05/2022 non testé !
      final String query =
"""
      UPDATE handicap_index AS UPD,
      (SELECT * FROM handicap_index
       WHERE HandicapPlayerid=?
       ORDER BY HandicapDate desc
       LIMIT 20) SEL
       SET UPD.HandicapExceptionalScoreReduction=?,
       UPD.HandicapScoreDifferential=UPD.HandicapScoreDifferential ?
       WHERE UPD.HandicapId=SEL.HandicapId
""";
            ps = conn.prepareStatement(query);
            ps.setInt(1, player.getIdplayer());
            ps.setDouble(2,esr);
            ps.setDouble(3,esr);
            utils.LCUtil.logps(ps); 
            int row = ps.executeUpdate();
                LOG.debug(" ESR modified rows = " + row);
            if (row != 0) {
                //  LOG.debug("before creditcard success msg");
               //  String msg =  LCUtil.prepareMessageBean("creditcard.success") + creditcard.getCreditCardHolder()
                 String msg = "update records for esr successfull";
                 LOG.debug(msg);
      //           LCUtil.showMessageInfo(msg);
                 return true;
             }else{
                   String msg = "NOT NOT Successful update, row = 0 "
                           + " player = " + player;
                   LOG.debug(msg);
                   LCUtil.showMessageFatal(msg);
                   return false;
                 } //end if
  }catch (SQLException sqle) {
            String msg = "££££ SQLException in " + methodName + sqle.getMessage() + " , SQLState = "
                    + sqle.getSQLState() + " , ErrorCode = " + sqle.getErrorCode()
                    + " player = " + player.getIdplayer();
            LOG.error(msg);
            LCUtil.showMessageFatal(msg);
            return false;
  } catch(Exception e) {
            String msg = "£££ Exception in " + methodName + e.getMessage();
            LOG.error(msg);
            LCUtil.showMessageFatal(msg);
            return false;
        } finally {
          //  DBConnection.closeQuietly(conn, null, null, ps);
            DBConnection.closeQuietly(null, null, null, ps); // new 14/08/2014
        }
 } //end method

void main() throws Exception, SQLException{
    Connection conn = new DBConnection().getConnection();
    Player player = new Player();
    player.setIdplayer(324713);
    Round round = new Round();
    round.setIdround(480);
    int esr = -1;
    boolean b = new update.UpdateExceptionalScoreReduction().update(player,esr, conn);
 //   round.setRoundDate(LocalDateTime.of(2019,Month.APRIL,01,0,0));
 //   round.setRoundQualifying("C");  // "C" = counting, N = non qualifying et Y = qualifying
     LOG.debug(" Voici le résultat : = " + b);
     DBConnection.closeQuietly(conn, null, null, null); 
}// end main
} //end class
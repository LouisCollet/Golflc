
package create;

import com.fasterxml.jackson.databind.ObjectMapper;
import entite.Round;
import entite.ScoreMatchplay;
import static interfaces.Log.LOG;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import utils.DBConnection;
import utils.LCUtil;




public class CreateScoreMatchplay {
    private static PreparedStatement ps = null;
     
 public boolean create(final ScoreMatchplay score, final Round round, final Connection conn) throws SQLException{
 try {
     LOG.debug(" ... entering createScoreMatchplay() ");
         LOG.debug("scorematchplay = " + score);
         LOG.debug("Round = " + round);

        final String query =
              "UPDATE round"
             + " SET RoundMatchplayResult = ?"
             + " WHERE idround = ?"
        ;
        ps = conn.prepareStatement(query);
        ObjectMapper om = new ObjectMapper();
        ps.setString(1, om.writeValueAsString(score));
   // where fields
        ps.setInt(2, round.getIdround());
        utils.LCUtil.logps(ps); 
        int row = ps.executeUpdate();
        if (row!= 0) {
            String msg = "Successful update score matchplay "
                                + " round = " + round.getIdround() + " score = " + score;
            LOG.debug(msg);
            LCUtil.showMessageInfo(msg);
            return true;
        }else{
            String msg = "NOT NOT Successful update,"
                                     + " round = " + round.getIdround();
  //                              + " player = " + player.getIdplayer()
//                                + " , Strokes = " + s; //c1[i]; // sc.get(i);
            LOG.debug(msg);
            LCUtil.showMessageFatal(msg);
            return false;
        }
 //            modify.ModifyMatchplayResult.modifyMPResult(round, player, result, conn);
 } catch (SQLException sqle) {
            String msg = "£££ SQLException in Insert or Update score = " + sqle.getMessage() + " , SQLState = "
                    + sqle.getSQLState() + " , ErrorCode = " + sqle.getErrorCode();
            LOG.error(msg);
            LCUtil.showMessageFatal(msg);
            return false;
        } catch (Exception e) {
            String msg = "£££ Exception in Insert or Update score = " + e.getMessage();
            LOG.error(msg);
            LCUtil.showMessageFatal(msg);
            return false;
        } finally {
          //  DBConnection.closeQuietly(conn, null, null, ps);
            DBConnection.closeQuietly(null, null, null, ps); // new 14/08/2014
        }

  } //end method
} // end class
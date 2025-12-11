
package create;

import entite.Player;
import entite.Round;
import entite.ScoreStableford;
import java.sql.Connection;
import java.sql.SQLException;
import utils.LCUtil;

public class CreateOrUpdateScoreStableford implements interfaces.Log{
  public boolean status(final ScoreStableford score, final Round round, final Player player,
            final Connection conn) throws SQLException{
 try{
        LOG.debug(" ... entering CreateOrUpdateScoreStableford() ...");
      int rows = new find.FindCountScore().find(conn, player, round, "rows");
         LOG.info("numbers of rows = " + rows);
       boolean b = false;
       if(rows == 0){
             LOG.info("This is an Insert " + rows);
             b = new create.CreateScoreStableford().create(score, round, player, conn); // mod 15/04/2022
       }else{
             LOG.info("This is a Modify " + rows);
           b = new update.UpdateScoreStableford().update(score, round, player, conn);
       }
    return b;
} catch (SQLException sqle) {
            String msg = "£££ SQLException in CreateOrModifyScoreStableford = " + sqle.getMessage() + " , SQLState = "
                    + sqle.getSQLState() + " , ErrorCode = " + sqle.getErrorCode();
            LOG.error(msg);
            LCUtil.showMessageFatal(msg);
            return false;
 } catch (Exception e) {
            String msg = "£££ Exception in CreateOrModifyScoreStableford = " + e.getMessage();
            LOG.error(msg);
            LCUtil.showMessageFatal(msg);
            return false;
 } finally { }
} //end method
} //end class
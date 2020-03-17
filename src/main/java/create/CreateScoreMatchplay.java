
package create;

//import entite.Player;
import entite.Round;
import entite.ScoreMatchplay;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import utils.DBConnection;
import utils.LCUtil;
/**
 *
 * @author collet
 */
public class CreateScoreMatchplay implements interfaces.Log{
    private static PreparedStatement ps = null;
 public void createAllScores(final ScoreMatchplay score, final Round round, final Connection conn) throws SQLException
{
    
 try {
     LOG.info(" ... entering createScoreMatchplay() ");
         LOG.info("scorematchplay = " + score.toString());
         LOG.info("Round = " + round);
        
 //        LOG.info("result = " + result );
        final String query
            = "UPDATE round"
            + " SET round.RoundScoreStringCompressed = compress(?),"
            + " RoundMatchplayResult=?,"
            + " RoundModificationDate=?"
            + " WHERE idround = ?"
        ;
        ps = conn.prepareStatement(query);
        String r = utils.LCUtil.array2DToString(score.getScoreMP4());
        score.setScoreString(r);
        ps.setString(1, score.getScoreString() );
            LOG.info("Update score : setString " );
 //       String result = score.getMatchplayResult();
            LOG.info("Result matchplay = "  + score.getMatchplayResult());
            // v"rifier s'il y a un résultat !
 //       score.setMatchplayResult(result);
        ps.setString(2, score.getMatchplayResult() );
        ps.setTimestamp(3, Timestamp.from(Instant.now()));
   // where fields
        ps.setInt(4, round.getIdround());
             //    String p = ps.toString();
            utils.LCUtil.logps(ps); 
        int row = ps.executeUpdate();
        if (row!= 0) {
   //         setShowButtonCreateScore(false);  // n'affiche plus le bouton bas ecran
  ////                      CourseController.setShowButtonCreateStatistics(true);  // affiche plus le bouton bas ecran
            String msg = "Successful update score matchplay "
                                + " round = " + round.getIdround();
            LOG.info(msg);
            LCUtil.showMessageInfo(msg);
        }else{
            String msg = "NOT NOT Successful update,"
                                     + " round = " + round.getIdround();
  //                              + " player = " + player.getIdplayer()
//                                + " , Strokes = " + s; //c1[i]; // sc.get(i);
            LOG.info(msg);
            LCUtil.showMessageFatal(msg);
        }
 //            modify.ModifyMatchplayResult.modifyMPResult(round, player, result, conn);
        }catch (NullPointerException npe) {
            String msg = "££££ NullPointerException in Insert or Update ScoreMatchplay = " + npe.getMessage();
            LOG.error(msg);
            LCUtil.showMessageFatal(msg);
        } catch (IndexOutOfBoundsException iobe) {
            String msg = "£££ IndexOutOfBoundsException in Insert or Update ScoreMatchplay = " + iobe.getMessage();
            LOG.error(msg);
            LCUtil.showMessageFatal(msg);
        }
// catch (MySQLIntegrityConstraintViolationException cv) {
//            String msg = "£££ MySQLIntegrityConstraintViolationException in Insert or Update Score = " + cv.getMessage();
//            LOG.error(msg);
//            LCUtil.showMessageFatal(msg);
    //return null;
//        }
 catch (SQLException sqle) {
            String msg = "£££ SQLException in Insert or Update score = " + sqle.getMessage() + " , SQLState = "
                    + sqle.getSQLState() + " , ErrorCode = " + sqle.getErrorCode();
            LOG.error(msg);
            LCUtil.showMessageFatal(msg);
        } catch (NumberFormatException nfe) {
            String msg = "£££ NumberFormatException in Insert or Update Score = " + nfe.getMessage();
            LOG.error(msg);
            LCUtil.showMessageFatal(msg);
        } catch (Exception e) {
            String msg = "£££ Exception in Insert or Update score = " + e.getMessage();
            LOG.error(msg);
            LCUtil.showMessageFatal(msg);
        } finally {
          //  DBConnection.closeQuietly(conn, null, null, ps);
            DBConnection.closeQuietly(null, null, null, ps); // new 14/08/2014
        }

  } //end method

} // end class

package modify;

import entite.Player;
import entite.Round;
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
public class ModifyMatchplayResult implements interfaces.Log
{

 public void modifyMPResult(final Round round, final Player player,
         final String result, final Connection conn) throws SQLException
 {
        PreparedStatement ps = null;
  try {
 //           LOG.info("starting ");
            LOG.info("Round ID = " + round.getIdround() );
            LOG.info("result = " + result);
            LOG.info("Player ID = " + player.getIdplayer() );

            final String query
              = "  UPDATE player_has_round" +
                "  SET player_has_round.Player_has_roundMatchplayResult = ?, Player_has_roundModificationDate = ? " +
                "  WHERE " +
                "       InscriptionIdPlayer = ?" +
                "   AND InscriptionIdRound = ?";
            
                    ps = conn.prepareStatement(query);
                    ps.setString(1, result);  //ici scoreStroke 
                    ps.setTimestamp(2, Timestamp.from(Instant.now()));
                    ps.setInt(3, player.getIdplayer());
                    ps.setInt(4, round.getIdround());
                     //    String p = ps.toString();
                        utils.LCUtil.logps(ps); 
                    int row = ps.executeUpdate();
                    if (row != 0) {
                        String msg = "Successful update : "
                                + " player = " + player.getIdplayer()
                                + " result  = " + result;
  
                        LOG.info(msg);
                        LCUtil.showMessageInfo(msg);
                    }else{
                        String msg = "NOT NOT Successful update,"
    //                            + " hole  = " + (i + 1)
                                + " player = " + player.getIdplayer();
                        LOG.info(msg);
                        LCUtil.showMessageFatal(msg);
                    }
       } //end try
        catch (NullPointerException npe) {
            String msg = "£££ NullPointerException in Insert or Update Score = " + npe.getMessage();
            LOG.error(msg);
            LCUtil.showMessageFatal(msg);
        } catch (IndexOutOfBoundsException iobe) {
            String msg = "£££ IndexOutOfBoundsException in Insert or Update Score = " + iobe.getMessage();
            LOG.error(msg);
            LCUtil.showMessageFatal(msg);
       }
  //   catch (MySQLIntegrityConstraintViolationException cv) {
     //       String msg = "£££ MySQLIntegrityConstraintViolationException in Insert or Update Score = " + cv.getMessage();
     //       LOG.error(msg);
     //       LCUtil.showMessageFatal(msg);
    //return null;
    //    } 
catch (SQLException sqle) {
            String msg = "££££ SQLException in Insert or Update score = " + sqle.getMessage() + " , SQLState = "
                    + sqle.getSQLState() + " , ErrorCode = " + sqle.getErrorCode()
                    + " player = " + player.getIdplayer();
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
 
  } //end class
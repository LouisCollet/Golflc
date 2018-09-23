
package create;

import entite.Handicap;
import entite.Player;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import utils.LCUtil;

/**
 *
 * @author collet new 24/06/2014
 */
public class CreateInitialHandicap implements interfaces.Log, interfaces.GolfInterface
{

    public boolean createHandicap(Connection conn, Player player, Handicap handicap, String batch) throws SQLException
    {
        PreparedStatement ps = null;
        int row = 0;
        try {
            LOG.info("-- Inserting initial player handicap for player = "  + player.getIdplayer()
                        + " Handicap   = " + handicap.getHandicapPlayer()
                        + " Handicap D = " + SDF.format(handicap.getHandicapStart() ) );
            String query = LCUtil.generateInsertQuery(conn, "handicap");
                //query = "INSERT INTO handicap VALUES (?,?,?,?,?,?)";
                ps = conn.prepareStatement(query);
                ps.setDate(1, LCUtil.getSqlDate(handicap.getHandicapStart()));
                ps.setString(2, DATE_END_HANDICAP); // date de fin fictive pour tous
                ps.setBigDecimal(3, handicap.getHandicapPlayer());
                ps.setInt(4, player.getIdplayer());
                // astuce : toujours round 1, (joué sur course 33 du club 999 !! fictif)
                ps.setString(5, "1"); // round
                ps.setTimestamp(6, LCUtil.getCurrentTimeStamp());
                 //    String p = ps.toString();
         //       p = ps.toString();
                utils.LCUtil.logps(ps); 
                row = ps.executeUpdate(); // write into database
                if (row != 0) {
                    String msg = "!! successful insert Handicap : "
                            + " <br/>ID = " + player.getIdplayer()
                            + " <br/>first = " + player.getPlayerFirstName()
                            + " <br/>last = " + player.getPlayerLastName()
                            + " <br/>date handicap = " + SDF.format(handicap.getHandicapStart() )
                            + " <br/>handicap = " + handicap.getHandicapPlayer();
                    LOG.info(msg);
                   LCUtil.showMessageInfo(msg);
                    if (! batch.equals("B"))
                        {LCUtil.showMessageInfo(msg);}
                  
                }else{
                    String msg = "!! NOT NOT successful insert Handicap : "
                            + " <br/>ID = " + player.getIdplayer()
                            + " <br/>first = " + player.getPlayerFirstName()
                            + " <br/>last = " + player.getPlayerLastName()
                            + " <br/>date handicap = " + handicap.getHandicapStart()
                            + " <br/>handicap = " + handicap.getHandicapPlayer();
                    LOG.error(msg);
                    LCUtil.showMessageInfo(msg);
                    if (! batch.equals("B"))
                        {LCUtil.showMessageFatal(msg);}
                    return false;
                }
return true;
        } // end try
  //      catch (MySQLIntegrityConstraintViolationException cv) {
  //          String msg = "£££ MySQLIntegrityConstraintViolationException in create Handicap = " + cv.getMessage();
  //          LOG.error(msg);
  //          if (! batch.equals("B"))
  //              {LCUtil.showMessageFatal(msg);}
  //          return false;
  //      }
        catch (SQLException sqle) {
            String msg = "£££ SQLException in create Handicap = " + sqle.getMessage() + " ,SQLState = "
                    + sqle.getSQLState() + " ,ErrorCode = " + sqle.getErrorCode();
            LOG.error(msg);
            if (! batch.equals("B"))
                {LCUtil.showMessageFatal(msg);}
            return false;
        } catch (NumberFormatException nfe) {
            String msg = "£££ NumberFormatException in create Handicap = " + nfe.getMessage();
            LOG.error(msg);
            if (! batch.equals("B"))
                {LCUtil.showMessageFatal(msg);}
            return false;
        } finally {
            utils.DBConnection.closeQuietly(null, null, null, ps);
        }
   //      return false;
    } //end createplayer

}

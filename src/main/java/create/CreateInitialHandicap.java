
package create;

import entite.Handicap;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import utils.LCUtil;

public class CreateInitialHandicap implements interfaces.Log, interfaces.GolfInterface{
  public boolean create(Connection conn, 
      //    Player player,
          Handicap handicap, String batch) throws SQLException{
        PreparedStatement ps = null;
        int row = 0;
        try {
           // LOG.info("-- Inserting initial player handicap for = "  + player.getIdplayer()
            LOG.info(" entering intitial player handicap = " + handicap.toString());
                     //   + " Handicap D = " + SDF.format(handicap.getHandicapStart() ) );
            String query = LCUtil.generateInsertQuery(conn, "handicap");
                //query = "INSERT INTO handicap VALUES (?,?,?,?,?,?)";
                ps = conn.prepareStatement(query);
                ps.setDate(1, LCUtil.getSqlDate(handicap.getHandicapStart()));
                ps.setString(2, DATE_END_HANDICAP); // date de fin fictive pour tous
                ps.setBigDecimal(3, handicap.getHandicapPlayer());
                ps.setInt(4, handicap.getPlayerIdplayer());
                // astuce : toujours round 1, (joué sur course 33 du club 999 !! fictif)
                ps.setString(5, "1"); // round
                ps.setTimestamp(6, Timestamp.from(Instant.now()));
                 utils.LCUtil.logps(ps); 
                row = ps.executeUpdate(); // write into database
                if (row != 0) {
                    String msg = "!! Successful insert Handicap : "
                        //    + " <br/>ID = " + player.getIdplayer()
                       //     + " <br/>first = " + player.getPlayerFirstName()
                       //     + " <br/>last = " + player.getPlayerLastName()
                            + " <br/>date handicap = " + SDF.format(handicap.getHandicapStart() )
                            + " <br/>handicap = " + handicap.getHandicapPlayer();
                    LOG.info(msg);
         //          LCUtil.showMessageInfo(msg);
                    if (! batch.equals("B")){
                        LOG.info("not batch");
         //               LCUtil.showMessageInfo(msg);
                    }
                  
                }else{
                    String msg = "!! NOT NOT successful insert Handicap : "
                    //        + " <br/>ID = " + player.getIdplayer()
                      //      + " <br/>first = " + player.getPlayerFirstName()
                     //       + " <br/>last = " + player.getPlayerLastName()
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
        catch (SQLException sqle) {
            String msg = "£££ SQLException in create Initial Handicap = " + sqle.getMessage() + " ,SQLState = "
                    + sqle.getSQLState() + " ,ErrorCode = " + sqle.getErrorCode();
            LOG.error(msg);
            if (! batch.equals("B"))
                {LCUtil.showMessageFatal(msg);}
            return false;
        } catch (Exception nfe) {
            String msg = "£££ tException in create Initial Handicap = " + nfe.getMessage();
            LOG.error(msg);
            if (! batch.equals("B"))
                {LCUtil.showMessageFatal(msg);}
            return false;
        } finally {
            utils.DBConnection.closeQuietly(null, null, null, ps);
        }
   //      return false;
    } //end createplayer

} // end class
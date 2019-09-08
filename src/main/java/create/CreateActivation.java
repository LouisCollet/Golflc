
package create;

import entite.Handicap;
import entite.Player;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.UUID;
import utils.LCUtil;
import static utils.LCUtil.printSQLException;

public class CreateActivation implements interfaces.Log, interfaces.GolfInterface{
 // question : pourquoi le handicap ?
    public  boolean create(Connection conn, Player player, Handicap handicap) throws Exception {
         PreparedStatement ps = null;
         int row = 0;
  try {
         UUID uuid = UUID.randomUUID();
               LOG.info("Universally Unique Identifier = " + uuid.toString());
         String url = utils.LCUtil.firstPartUrl();
     //    String href = "http://" + host + ":" + port + uri + "/activation_check.xhtml?key=" + uuid.toString();       
        String href = url + "/activation_check.xhtml?faces-redirect=true&uuid=" + uuid.toString(); 
         //    String href = "http://localhost:8080/GolfNew-1.0-SNAPSHOT/activation_check.xhtml?key=" + uuid.toString();  
      LOG.info("** href for activation = " + href);   
 
        String msg = "-- Inserting initial Activation for player = " + player.getIdplayer()
                        + "Handicap   = " + handicap.getHandicapPlayer()
                        + "Handicap start = " + handicap.getHandicapStart();
        LOG.info(msg);
        LCUtil.showMessageInfo(msg);
        
        String query = LCUtil.generateInsertQuery(conn, "activation");
               //query = "INSERT INTO activation VALUES (?,?,?)";
        ps = conn.prepareStatement(query);
        ps.setString(1, uuid.toString()); // ActivationKey
        ps.setInt(2, player.getIdplayer());
        ps.setString(3, player.getPlayerLanguage() ); // new 20/12/2014
        ps.setTimestamp(4, LCUtil.getCurrentTimeStamp());
   //     ps.setDate(5,LCUtil.toSqlDate(sdf.parse("2016-01-02")));
         //    String p = ps.toString();
           utils.LCUtil.logps(ps);
        row = ps.executeUpdate(); // write into database
        if (row != 0) {
             msg = "!! successful insert Activation : "
                  + " <br/>ID = " + player.getIdplayer()
                  + " <br/>first = " + player.getPlayerFirstName()
                  + " <br/>last = " + player.getPlayerLastName()
                  + " <br/>date handicap = " + handicap.getHandicapStart()
                  + " <br/>handicap = " + handicap.getHandicapPlayer();
            LOG.info(msg);
            LCUtil.showMessageInfo(msg);
               boolean b =  new mail.ActivationMail().sendMailAccountCreated(player);   //envoi du mail
            return true;
            } else {
               msg = "!! NOT  NOT successful insert Activation : "
                 + " <br/>ID = " + player.getIdplayer()
                 + " <br/>first = " + player.getPlayerFirstName()
                 + " <br/>last = " + player.getPlayerLastName()
                 + " <br/>date handicap = " + handicap.getHandicapStart()
                 + " <br/>handicap = " + handicap.getHandicapPlayer();
                    LOG.error(msg);
                    LCUtil.showMessageFatal(msg);
                    return false;
                } // end if
   } catch (SQLException sqle) {
            printSQLException(sqle); // new 13-05-2019
            String msg = "£££ SQLException in CreateActivation = " + sqle.getMessage() + " ,SQLState = "
                    + sqle.getSQLState() + " ,ErrorCode = " + sqle.getErrorCode();
            LOG.error(msg);
            LCUtil.showMessageFatal(msg);
            return false;
   } catch (NumberFormatException nfe) {
            String msg = "£££ NumberFormatException in CreateActivation = " + nfe.getMessage();
            LOG.error(msg);
            LCUtil.showMessageFatal(msg);
            return false;
        } finally {
         //  DBConnection.closeQuietly(conn, null, null, ps);
         } 

} // end method
} // end Class
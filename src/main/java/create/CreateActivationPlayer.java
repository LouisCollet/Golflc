
package create;

import entite.Handicap;
import entite.Player;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.UUID;
import lc.golfnew.LanguageController;
import utils.LCUtil;
import static utils.LCUtil.printSQLException;

public class CreateActivationPlayer implements interfaces.Log, interfaces.GolfInterface{
 // question : pourquoi le handicap ?
    public  boolean create(Connection conn, Player player, Handicap handicap) throws Exception {
         PreparedStatement ps = null;
         int row = 0;
  try {
      LOG.info("entering CreateActivationPlayer");
         String msg = "-- Inserting initial Activation for new player = " + player.getIdplayer()
                        + "Handicap   = " + handicap.getHandicapPlayer()
                        + "Handicap start = " + handicap.getHandicapStart();
        LOG.info(msg);
    //    LCUtil.showMessageInfo(msg);
        
        String query = LCUtil.generateInsertQuery(conn, "activation");
               //query = "INSERT INTO activation VALUES (?,?,?)";
        ps = conn.prepareStatement(query);
        String uuid = UUID.randomUUID().toString();
           LOG.info("Universally Unique Identifier = " + uuid);
        ps.setString(1, uuid); // ActivationKey
        ps.setInt(2, player.getIdplayer());
        ps.setString(3, player.getPlayerLanguage() );
        ps.setTimestamp(4, Timestamp.from(Instant.now()));
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
                  + " <br/>handicap = " + handicap.getHandicapPlayer()
                  + " <br/>language = " + player.getPlayerLanguage()
                  + " <br/>you will receive a mail, and click on it = "
                     ;
            LOG.info(msg);
      //      LCUtil.showMessageInfo(msg);
            
    
            String href = utils.LCUtil.firstPartUrl()
                 + "/activation_check.xhtml?uuid=" 
                 + uuid //; // mod 02-12-2018
                 + "&firstname=" + player.getPlayerFirstName()
                 + "&lastname=" + player.getPlayerLastName()
                 + "&language=" + player.getPlayerLanguage();
            href = href.replaceAll(" ","%20"); // pour prénoms composés séparés par un blanc
                LOG.info("** href for activation new player = " + href);
            LanguageController.setLanguage(player.getPlayerLanguage());
               if(new mail.ActivationMail().sendMailAccountCreated(player, href)){ // 
                   msg = LCUtil.prepareMessageBean("create.registration.mail"); // mail send
                   LOG.info(msg);
                   utils.LCUtil.showMessageInfo(msg);
               }   //envoi du mail
               

               
               
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
   } catch (Exception nfe) {
            String msg = "£££ Exception in CreateActivation = " + nfe.getMessage();
            LOG.error(msg);
            LCUtil.showMessageFatal(msg);
            return false;
        } finally {
         //  DBConnection.closeQuietly(conn, null, null, ps);
         } 

} // end method
} // end Class
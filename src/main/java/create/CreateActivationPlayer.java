
package create;

import Controllers.LanguageController;
import entite.LatLng;
import entite.Player;
import static interfaces.Log.LOG;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;
import utils.DBConnection;
import utils.LCUtil;
import static utils.LCUtil.printSQLException;

public class CreateActivationPlayer implements interfaces.Log, interfaces.GolfInterface{
  public boolean create(Connection conn, Player player) throws Exception {
         PreparedStatement ps = null;
         int row = 0;
  try {
      LOG.debug("entering CreateActivationPlayer");
         String msg = "-- Inserting initial Activation for new player = " + player.getIdplayer()
                 ;
        LOG.debug(msg);
    //    LCUtil.showMessageInfo(msg);
        
        final String query = LCUtil.generateInsertQuery(conn, "activation");
               //query = "INSERT INTO activation VALUES (?,?,?)";
        ps = conn.prepareStatement(query);
        String uuid = UUID.randomUUID().toString();
           LOG.debug("Universally Unique Identifier uuid = " + uuid);
        ps.setString(1, uuid); // ActivationKey
        ps.setInt(2, player.getIdplayer());
        ps.setString(3, player.getPlayerLanguage() );
        ps.setTimestamp(4, Timestamp.from(Instant.now()));
        utils.LCUtil.logps(ps);
        row = ps.executeUpdate(); // write into database
        if(row != 0) {
             msg = "!! successful insert Activation : "
                  + " <br/>ID = " + player.getIdplayer()
                  + " <br/>first = " + player.getPlayerFirstName()
                  + " <br/>last = " + player.getPlayerLastName()
                  + " <br/>language = " + player.getPlayerLanguage()
                  + " <br/>you will receive a mail, and click on it = "
                     ;
            LOG.debug(msg);
      //      LCUtil.showMessageInfo(msg);
            String href = utils.LCUtil.firstPartUrl() //  = "http://localhost:8080/GolfWfly-1.0-SNAPSHOT"
                 + "/activation_check.xhtml?uuid=" 
                 + uuid //; // mod 02-12-2018
                 + "&firstname=" + player.getPlayerFirstName().replaceAll(" ","%20")// pour prénoms composés séparés par un blanc
                 + "&lastname=" + player.getPlayerLastName().replaceAll(" ","%20")
                 + "&language=" + player.getPlayerLanguage();
        //    href = href.replaceAll(" ","%20"); 
                LOG.debug("** href for activation new player = " + href);
            LanguageController.setLanguage(player.getPlayerLanguage());
            if(new mail.ActivationMail().sendMailAccountCreated(player, href)){
                msg = LCUtil.prepareMessageBean("create.registration.mail"); // mail send
                   LOG.debug(msg);
                utils.LCUtil.showMessageInfo(msg);
                return true;
             }else{
                msg ="ERROR mail not started";
                LOG.error(msg);
                utils.LCUtil.showMessageFatal(msg);
                return false;
               }
          }else{  // error row = 0
               msg = "!! NOT  NOT successful insert Activation : "
                 + " <br/>ID = " + player.getIdplayer()
                 + " <br/>first = " + player.getPlayerFirstName()
                 + " <br/>last = " + player.getPlayerLastName();
        //         + " <br/>date handicap = " + handicap.getHandicapStart()
        //         + " <br/>handicap = " + handicap.getHandicapPlayer();
                    LOG.error(msg);
                    LCUtil.showMessageFatal(msg);
                    return false;
             } // end else
  }catch(SQLException sqle) {
            printSQLException(sqle); // new 13-05-2019
            String msg = "£££ SQLException in CreateActivation = " + sqle.getMessage() + " ,SQLState = "
                    + sqle.getSQLState() + " ,ErrorCode = " + sqle.getErrorCode();
            LOG.error(msg);
            LCUtil.showMessageFatal(msg);
            return false;
   }catch (Exception nfe){
            String msg = "£££ Exception in CreateActivation = " + nfe.getMessage();
            LOG.error(msg);
            LCUtil.showMessageFatal(msg);
            return false;
   }finally {
         //  DBConnection.closeQuietly(conn, null, null, ps);
         } 
} // end method
  
 void main() throws SQLException, Exception {
   Connection conn = new DBConnection().getConnection();
   try{
            Player player = new Player();
        player.setIdplayer(111111); // 528951 529952
        player.setPlayerFirstName("first test activation");
        player.setPlayerLastName("last test activation ");
     //   player.setPlayerBirthDate(SDF.parse("01/03/2000"));
        player.setPlayerBirthDate(LocalDateTime.parse("2018-11-03T12:45:30"));  // mod 13-04-2022
    //    TimeZone gtz = new TimeZone();
   //     gtz.setTimeZoneId("Europe/Brussels");
        player.getAddress().setZoneId("Europe/Brussels");
        player.setPlayerHomeClub(101);
        player.getAddress().setCity("Brussels");
        player.setPlayerGender("M");
        player.setPlayerLanguage("es");
     //   player.getAddress().setCountry("US");
        // mod 22-12-2022
        player.getAddress().getCountry().setCode("US");
        player.getAddress().setLatLng(new LatLng(Double.parseDouble("50.8262271"), Double.parseDouble("4.3571382")));
  /// à modifier      player.setPlayerLatLng(new LatLng(Double.parseDouble("50.8262271"), Double.parseDouble("4.3571382"))); // amazone 55
            
      //      Handicap handicap = new Handicap();
            boolean b = new create.CreateActivationPlayer().create(conn, player);
            LOG.debug("from main, CreateBlocking = " + b);
    }catch (Exception e){
            String msg = "££ Exception in main CreateBlocking = " + e.getMessage();
            LOG.error(msg);
            //      LCUtil.showMessageFatal(msg);
    }finally{
            DBConnection.closeQuietly(conn, null, null, null);
    }
 } // end main
} // end Class
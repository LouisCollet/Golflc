package create;

import entite.Player;
import static interfaces.Log.LOG;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.UUID;
import utils.DBConnection;
import utils.LCUtil;

public class CreateActivationPassword implements interfaces.GolfInterface{
    public  boolean create(final Connection conn, final Player player) throws Exception     {
         PreparedStatement ps = null;
     //    int row = 0;
  try {
        final String query = LCUtil.generateInsertQuery(conn, "activation");
        ps = conn.prepareStatement(query);
        String uuid = UUID.randomUUID().toString();
    //       LOG.debug("Universally Unique Identifier = " + uuid.toString());
        ps.setString(1, uuid); // ActivationKey
        ps.setInt(2, player.getIdplayer());
        ps.setString(3, player.getPlayerLanguage() );
        ps.setTimestamp(4, Timestamp.from(Instant.now()));
        utils.LCUtil.logps(ps);
        int row = ps.executeUpdate(); // write into database
        if (row != 0){
            LocalDateTime localDateTime = LocalDateTime.now().plusMinutes(10);
            Date date = Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
            LOG.debug("date plus 10 = " +  date);
            LOG.debug("date in milli ? = " + date.getTime());
            String href = utils.LCUtil.firstPartUrl() + "/password_check.xhtml" 
                    + "?uuid=" + uuid
                    + "&firstname=" + player.getPlayerFirstName()
                    + "&lastname=" + player.getPlayerLastName()
                    + "&language=" + player.getPlayerLanguage()
                    + "&time=" + date // à formater ?
                    + "&millis=" + date.getTime() // à formater ?
                    ;
            href = href.replaceAll(" ","%20"); // prénom séparé par des blancs !
                LOG.debug("** href for activation password = " + href);
            if(new mail.ResetPasswordMail().send(player, href)){ // mail send
                 String msg = LCUtil.prepareMessageBean("create.reset.mail"); 
                 LOG.debug(msg);
                 utils.LCUtil.showMessageInfo(msg);
            }
     //           LOG.debug("Status resetpasswordmail = " + b);
             String msg = "!! successful insert Activation for Password : "
                  + " <br/>ID = " + player.getIdplayer()
                  + " <br/>first = " + player.getPlayerFirstName()
                  + " <br/>last = " + player.getPlayerLastName();
                LOG.debug(msg);
          //   LCUtil.showMessageInfo(msg);
            return true;
         } else {
               String msg = "!! NOT  NOT successful insert Activation for Password : "
                 + " <br/>ID = " + player.getIdplayer()
                 + " <br/>first = " + player.getPlayerFirstName()
                 + " <br/>last = " + player.getPlayerLastName();
                    LOG.error(msg);
                    LCUtil.showMessageFatal(msg);
                    return false;
                } // end if
        } catch (SQLException sqle) {
            String msg = "£££ SQLException in create ActivationPassword = " + sqle.getMessage() + " ,SQLState = "
                    + sqle.getSQLState() + " ,ErrorCode = " + sqle.getErrorCode();
            LOG.error(msg);
            LCUtil.showMessageFatal(msg);
            return false;
        } catch (Exception e) {
            String msg = "£££ Exception in createActivationPassword = " + e.getMessage();
            LOG.error(msg);
            LCUtil.showMessageFatal(msg);
            return false;
        } finally {
           utils.DBConnection.closeQuietly(null, null, null, ps);
         } 
} // end method
    
     void main() throws SQLException, Exception {
   Connection conn = new DBConnection().getConnection();
   try{
            Player player = new Player();
            player.setIdplayer(324713);
            player.setPlayerFirstName("Jon");
            player.setPlayerLastName("Rahm");
            boolean b = new create.CreateActivationPassword().create(conn, player);
            LOG.debug("from main, CreateActivationPassword = " + b);
    }catch (Exception e){
            String msg = "££ Exception in main CreateActivationPassword = " + e.getMessage();
            LOG.error(msg);
            //      LCUtil.showMessageFatal(msg);
    }finally{
            DBConnection.closeQuietly(conn, null, null, null);
    }
    } // end main//
    
} // end Class
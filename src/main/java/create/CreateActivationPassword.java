package create;

import entite.Player;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.UUID;
import utils.LCUtil;

public class CreateActivationPassword implements interfaces.Log, interfaces.GolfInterface{
    public  boolean create(Connection conn, Player player) throws Exception     {
         PreparedStatement ps = null;
         int row = 0;
      //   UUID uuid = UUID.randomUUID();
  try {
        String query = LCUtil.generateInsertQuery(conn, "activation");
               //query = "INSERT INTO handicap VALUES (?,?,?)";
        ps = conn.prepareStatement(query);
        UUID uuid = UUID.randomUUID();
    //       LOG.info("Universally Unique Identifier = " + uuid.toString());
        ps.setString(1, uuid.toString()); // ActivationKey
        ps.setInt(2, player.getIdplayer());
        ps.setString(3, player.getPlayerLanguage() ); // new 20/12/2014
        ps.setTimestamp(4, LCUtil.getCurrentTimeStamp());
        utils.LCUtil.logps(ps);
        row = ps.executeUpdate(); // write into database
        if (row != 0) {
        /*
            String url = utils.LCUtil.firstPartUrl();
     //    String href = "http://" + host + ":" + port + uri + "/activation_check.xhtml?key=" + uuid.toString();       
            String href = url + "/password_check.xhtml?uuid=" + uuid.toString() //; // mod 02-12-2018
                          + "&firstname=" + player.getPlayerFirstName()
                          + "&lastname=" + player.getPlayerLastName()
                          + "&language=" + player.getPlayerLanguage();
            href = href.replaceAll(" ","%20");
                LOG.info("** href for activation password after replace= " + href);  
             //        UnicodeEscaper ue = UnicodeEscaper.above(0);
    //        String result = ue.translate(player.getPlayerFirstName());
    //            LOG.info("**unicode de firstname = " + result);          
    //   href = StringEscapeUtils.escapeHtml4(href);
    //    LOG.info("**result for activation password  after escapeHtml = " + href);
   //    result = URLEncoder.encode(href, "UTF-8"); // new 03-12-2018
   //          LOG.info("**result for activation password  after encode = " + result);   
   //  result = StringEscapeUtils.escapeHtml4(href)*/
    // send a mail 
             mail.ResetPasswordMail rp = new mail.ResetPasswordMail();
             boolean b = rp.sendMail(player, uuid.toString());
             
             String msg = LCUtil.prepareMessageBean("create.reset.mail"); 
        //     String msg = "-- We just send you an mail for resetting your Password, please use it within the 10 minutes ... = " ;
             LOG.info(msg);
             utils.LCUtil.showMessageInfo(msg);
   
             msg = "!! successful insert Activation for Password : "
                  + " <br/>ID = " + player.getIdplayer()
                  + " <br/>first = " + player.getPlayerFirstName()
                  + " <br/>last = " + player.getPlayerLastName();
                LOG.info(msg);
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
} // end Class
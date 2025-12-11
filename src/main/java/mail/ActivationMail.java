package mail;

import entite.Club;
import entite.Player;
import entite.Round;
import static interfaces.GolfInterface.SDF_TIME;
import static interfaces.GolfInterface.ZDF_TIME;
import static interfaces.Log.LOG;
import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.ArrayList;
import jakarta.mail.MessagingException;
import static utils.LCUtil.showMessageFatal;

public class ActivationMail{

    public Boolean sendMailAccountCreated(Player player, String href) throws MessagingException, Exception {
        LOG.debug("entering sendMailAccountCreated for player = " + player);
         LOG.debug("** href/url for activation = " + href);
     String msg =
                  " <br/>Welcome to GolfLC! at "
            //    + SDF_TIME.format(new java.util.Date() )
                + LocalDateTime.now().format(ZDF_TIME)
                + " <br/> Thanks for signing up!"
                + " <br/> Your account has been created, but before you can login with the following credentials, you have to activate your account."
                + " <br/><b>ID         = </b>" + player.getIdplayer()
                + " <br/><b>First Name = </b>" + player.getPlayerFirstName()
                + " <br/><b>Last Name  = </b>" + player.getPlayerLastName()
                + " <br/><b>Language   = </b>" + player.getPlayerLanguage()
                + " <br/><b>City       = </b>" + player.getAddress().getCity()
                + " <br/><b>Email      = </b>" + player.getPlayerEmail()
                + " <br/> Please click here to activate your account: "
                + " <a href=" + href + "> to activate your account</a>"
                + " <br/> Thank you !"
                + "<b><font color='red';size='12'> WITHIN THE 10 MINUTES</b></font>"
              
                + " <br/> The GolfLC team"
                    ; 
     //           LOG.debug(msg);
            String sujet = "Activate Your Account for GolfLC";
            String to = "louis.collet@skynet.be";
            Path path = null;
            boolean b = new mail.SendEmail().sendHtmlMail(sujet,msg,to,path,
                    path, player.getPlayerLanguage());
                LOG.debug("sendMailAccountCreated status = " + b);
return b;
}
    public Boolean sendMailActivationOK(Player player) throws MessagingException, Exception {

            String href = utils.LCUtil.firstPartUrl() + "/login.xhtml";
            String sujet = "Succesfull activation to golflc !!!";
            String msg ="ok with your activation !!"
                + " <br/><b>ID         = </b>" + player.getIdplayer()
                + " <br/><b>First Name = </b>" + player.getPlayerFirstName()
                + " <br/><b>Last Name  = </b>" + player.getPlayerLastName()
                + " <br/><b>Language   = </b>" + player.getPlayerLanguage()
                + " <br/><b>City       = </b>" + player.getAddress().getCity()
                + " <br/><b>Email      = </b>" + player.getPlayerEmail()
                + " <br> for a connection to the application,"
                + " <br>click on the next url : "
                + " <a href=" + href + ">"
                + "Click for connection</a>"
                + " <br/> The GolfLC team"
                ;
                    // à modifier utilier <href ....>
                    //  msg = msg + "http://localhost:8080/GolfNew-1.0-SNAPSHOT/login.xhtml";
                     
 // à mofifier             //       <a href=" + href + ">"
                     String to = "louis.collet@skynet.be";
                     Path path = null;
                     boolean b = new mail.SendEmail().sendHtmlMail(sujet,msg,to,path,
                             path, player.getPlayerLanguage());
                        LOG.debug("HTML Mail status = " + b);
     return b;
    }
    
    void main() throws IOException {
  try{
      // not working error compilation 
      Player player = new Player();
      player.setIdplayer(456783);  // muntingh
      player.setPlayerLastName("Muntingh");
      player.setPlayerEmail("theo.muntingh@skynet.be");
      Player player2 = new Player();
      player2.setIdplayer(2014101);  // muntingh
      Player player3 = new Player();
      player3.setIdplayer(2014102);  
      ArrayList<Player> p = new ArrayList<>();   // transform player2 in list<player<    
      p.add(player2);
      p.add(player3);
      player.setDroppedPlayers(p);
 
      Player invitedBy = new Player();
      invitedBy.setIdplayer(324713);
      player.setPlayerLastName("Collet");

      Club club = new Club();
      club.setClubName("Cabopino");

  //    Course course = new Course();
 
      Round round = new Round(); 
      round.setRoundDate(LocalDateTime.of(2018, Month.NOVEMBER, 17, 12, 15));
      round.setRoundGame("round game : STABLEFORD");
      round.setPlayersString("inscrits précédemment : Corstjens, Bauer");

    new ActivationMail().sendMailActivationOK(player);
    String to = "louis.collet@skynet.be";
     Path path = null;
    boolean b = new mail.SendEmail().sendHtmlMail("sujet de test from main","message du mail",to,path,
            path, player.getPlayerLanguage());
       LOG.debug("HTML Mail status = " + b);
   } catch (Exception e) {
            String msg = "Â£Â£ Exception in main = " + e.getMessage();
            LOG.error(msg);
            showMessageFatal(msg);
   }
   } // end main//

} // end class
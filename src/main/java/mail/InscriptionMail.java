
package mail;

import entite.Club;
import entite.Course;
import entite.Player;
import entite.Round;
import static interfaces.GolfInterface.ZDF_TIME;
import static interfaces.GolfInterface.ZDF_TIME_HHmm;
import static interfaces.Log.LOG;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import jakarta.mail.MessagingException;
import static utils.LCUtil.showMessageFatal;
import static utils.LCUtil.showMessageInfo;

public class InscriptionMail{

public Boolean create(Player player, Player invitedBy, Round round, Club club, Course course )
        throws MessagingException, Exception {
        LOG.debug("entering InscriptionMail.create");
try{
        String sujet = "Your Round Inscription via GolfLC";
        String mail = 
                  " <br/>Inscription Confirmation - GolfLC!"
                + " <br/> Round Game   = " + round.getRoundGame()
                + " <br/> Round Date   = " + round.getRoundDate().format(ZDF_TIME_HHmm)
                + " <br/> Autres participants connus  : " + round.getPlayersString()
                + " <br/> Course Name  = " + course.getCourseName()
                + " <br/> Club Name    = " + club.getClubName()
                + " <br/> Club City    = " + club.getAddress().getCity()
                + " <br/><b>ID         = </b>" + player.getIdplayer()
                + " <br/><b>First Name = </b>" + player.getPlayerFirstName()
                + " <br/><b>Last Name  = </b>" + player.getPlayerLastName()
                + " <br/><b>Language   = </b>" + player.getPlayerLanguage()
                + " <br/><b>City       = </b>" + player.getAddress().getCity()
                + " <br/><b>Email      = </b>" + player.getPlayerEmail()
                + " <br/><b>Invited by     = </b>" + invitedBy.getPlayerLastName() + ", " + invitedBy.getPlayerFirstName()
 + " <br/>En utilisant le fichier .ics attaché à ce message, vous pouvez ajouter ce rendez-vous à votre agenda"
                + " <br/> Thank you !"
                + " <br/> The GolfLC team"
     //           + " <br/>" + LocalDateTime.now().format(ZDF_TIME)
                    ; 
                String to = "louis.collet@skynet.be";
    // insérer ici le ics
          Path pathICS = new utils.IcalGenerator().create(player, invitedBy, round, club, course, true);
    //                LOG.debug("pathICS = " + pathICS);
             LOG.debug("Size pathICS = " + Files.size(pathICS));
          String s = mail.replaceAll("<br/>","\n").replaceAll("<b>","").replaceAll("</b>","");
          Path pathQRC = utils.QRCodeGenerator.manageQR(s);
            LOG.debug("Size pathQRC = " + Files.size(pathQRC));

          if(new mail.SendEmail().sendHtmlMail(sujet,mail,to,pathICS,pathQRC,player.getPlayerLanguage())){
   //           LOG.debug("HTML Mail status = " + b);
             String msg = "Vous allez recevoir un mail de confirmation de votre inscription ";
             LOG.info(msg);
             showMessageInfo(msg);
             return true;
          }else{
            String msg = "mail de confirmation NOT sent !!";
            LOG.error(msg);
            showMessageFatal(msg);
              
              
              return false;
          }


}catch (Exception ex){
    String msg = "Exception in InscriptionMail.create " + ex;
    LOG.error(msg);
    showMessageFatal(msg);
    return false;
}finally{  //      return false;
     
 }
} // end method create


public Boolean delete(Player player, Round round, Club club, Course course) throws MessagingException, Exception {
        LOG.debug("entering InscriptionMail.delete");
 try{       
    String sujet = "Cancellation of your Round Inscription in GolfLC";
    String mail =
                  " <br/>Annulation Confirmation - GolfLC!"
                + " <br/>" + LocalDateTime.now().format(ZDF_TIME)
                + " <br/> Round Game   = " + round.getRoundGame()
                + " <br/> Round Date   = " + round.getRoundDate().format(ZDF_TIME_HHmm)
                + " <br/> Course Name  = " + course.getCourseName()
                + " <br/> Club Name    = " + club.getClubName()
                + " <br/> Club City    = " + club.getAddress().getCity()
                + " <br/><b>ID         = </b>" + player.getIdplayer()
                + " <br/><b>First Name = </b>" + player.getPlayerFirstName()
                + " <br/><b>Last Name  = </b>" + player.getPlayerLastName()
                + " <br/><b>Language   = </b>" + player.getPlayerLanguage()
                + " <br/><b>City       = </b>" + player.getAddress().getCity()
                + " <br/><b>Email      = </b>" + player.getPlayerEmail()
                + " <br/> Thank you !"
                + " <br/> The GolfLC team"
                    ; 

 //   FileOutputStream fos = new mail.FileICS().create(player, player, round, club, course, false);
    Path pathICS = new utils.IcalGenerator().create(player, player, round, club, course, false);
        LOG.debug("PathICS = " + pathICS);
// tester résultat ?
 //  String data2 = LocalDateTime.now() + "<br/> <bold> from courseController - sendmailtest Hello ! This is golfLC, the famous application v2";
  //   Path path2 = utils.QRCodeGenerator.manageQR(data, path);
    Path pathQRC = utils.QRCodeGenerator.manageQR(mail);
    String to = "louis.collet@skynet.be";
  //  Path pathQRC = null;
    boolean b = new mail.SendEmail().sendHtmlMail(sujet,mail,to,
      //      "ADD_ICS",
            pathICS, // new 07-10-2021
            pathQRC, player.getPlayerLanguage());
       LOG.debug("HTML Mail status = " + b);
return b;

}catch (Exception ex){
    String msg = "Exception in InscriptionMail.delete " + ex;
    LOG.error(msg);
    showMessageFatal(msg);
    return false;
}finally{  //      return false;
     
 }

} //end method




    } // end Class
//} // end class
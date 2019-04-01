/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mail;

import entite.Club;
import entite.Course;
import entite.ECourseList;
import entite.Player;
import entite.Round;
import static interfaces.GolfInterface.SDF_TIME;
import static interfaces.GolfInterface.ZDF_TIME_HHmm;
import static interfaces.Log.LOG;
import java.io.FileOutputStream;
import java.util.List;
import javax.mail.MessagingException;
import utils.LCUtil;

public class CancellationMail {
    
 public Boolean dispatch(List<ECourseList> ecl) throws MessagingException, Exception {
         String CLASSNAME2 = Thread.currentThread().getStackTrace()[1].getClassName(); 
 try{
     LOG.info("entering ... " + CLASSNAME2 );
     // boucler sur une liste
     LOG.info("nobe de cancellations = " + ecl.size());
         for(int i = 0; i < ecl.size(); i++) {
                LOG.info("i = " + i);
            String clubName = ecl.get(i).Eclub.getClubName();
                LOG.info("courseName  = " + clubName);    
            String courseName = ecl.get(i).Ecourse.getCourseName();
                LOG.info("courseName  = " + courseName);
            String roundDate = ecl.get(i).Eround.getRoundDate().toString();
                LOG.info("roundDate  = " + roundDate);
            String playerName = ecl.get(i).Eplayer.getPlayerLastName();
                LOG.info("playerName  = " + playerName);
            sendMail(ecl.get(i).Eplayer, ecl.get(i).Eplayer, ecl.get(i).Eround, ecl.get(i).Eclub,ecl.get(i).Ecourse);
        }
                  return true;       
     

}catch (Exception ex){
    String msg = "Exception in " + CLASSNAME2 + " / "  + ex;
    LOG.error(msg);
    LCUtil.showMessageFatal(msg);
    return false;
}
}
    public Boolean sendMail(Player player, Player invitedBy, Round round, Club club,Course course ) throws MessagingException, Exception {
{
        LOG.info("entering sendCancellattionMail");

        String sujet = "Your Round Cancellation via GolfLC";
                String Smail = 
                  " <br/>Cancellation notification - GolfLC!"
            
                + " <br/> Round Game   = " + round.getRoundGame()
                + " <br/> Round Date   = " + round.getRoundDate().format(ZDF_TIME_HHmm)
                + " <br/> Autres participants connus  : " + round.getPlayersString()
                + " <br/> Course Name  = " + course.getCourseName()
                + " <br/> Club Name    = " + club.getClubName()
                + " <br/> Club City    = " + club.getClubCity()
                + " <br/><b>ID         = </b>" + player.getIdplayer()
                + " <br/><b>First Name = </b>" + player.getPlayerFirstName()
                + " <br/><b>Last Name  = </b>" + player.getPlayerLastName()
                + " <br/><b>Language   = </b>" + player.getPlayerLanguage()
                + " <br/><b>City       = </b>" + player.getPlayerCity()
                + " <br/><b>Email      = </b>" + player.getPlayerEmail()
                + " <br/><b>Invited by     = </b>" + invitedBy.getPlayerLastName() + ", " + invitedBy.getPlayerFirstName()
 + " <br/>En utilisant le fichier .ics attaché à ce message, vous pouvez ajouter ce rendez-vous à votre agenda"
                + " <br/> Thank you !"
                + " <br/> The GolfLC team"
                + " <br/>" + SDF_TIME.format(new java.util.Date() )
                    ; 
                String to = "louis.collet@skynet.be";
         // insérer ici le ics
                mail.InscriptionICS im = new mail.InscriptionICS();
                FileOutputStream fos = im.createInscriptionMailICS(player, invitedBy, round, club, course, true);
                    LOG.info("fileoutputstrean fos = " + fos.toString());
         // a faire tester si pas null ??
                utils.SendEmail sm = new utils.SendEmail();
                boolean b = sm.sendHtmlMail(sujet,Smail,to,"INSCRIPTION");
                    LOG.info("HTML Mail status = " + b);
return b;
}
    }
} // end class
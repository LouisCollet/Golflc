/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mail;

import entite.Club;
import entite.Course;
import entite.Player;
import entite.Round;
import static interfaces.GolfInterface.SDF_TIME;
import static interfaces.GolfInterface.ZDF_TIME_HHmm;
import static interfaces.Log.LOG;
import java.io.FileOutputStream;
import javax.mail.MessagingException;

public class InscriptionMail {

    public Boolean sendInscriptionMail(Player player, Player invitedBy, Round round, Club club,Course course ) throws MessagingException, Exception {
{
        LOG.info("entering sendInscriptionMail");
        
        
        String sujet = "Your Round Inscription via GolfLC";
                String Smail = 
                  " <br/>Inscription Confirmation - GolfLC!"
            
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
                FileOutputStream fos = im.createInscriptionMailICS(player, invitedBy, round, club, course);
                    LOG.info("fileoutputstrean fos = " + fos.toString());
         // a faire tester si pas null ??
                utils.SendEmail sm = new utils.SendEmail();
                String type = "INSCRIPTION";
                boolean b = sm.sendHtmlMail(sujet,Smail,to,type);
                    LOG.info("HTML Mail status = " + b);

return b;
}

    
    }
} // end class

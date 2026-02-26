/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package mailtest;


import static interfaces.Log.LOG;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;
import mailtest.MailMessage;
import mailtest.PersistentMailService;

import java.io.Serializable;

@Named
@ViewScoped
public class MailBean implements Serializable {

    private static final long serialVersionUID = 1L;

    private static final PersistentMailService mailService =
            new PersistentMailService("mailQueue.dat");

    public void sendTestMail() {
        LOG.debug("entering sendTestMail");
        MailMessage msg = new MailMessage(
                "Test Mail Persistant",
                "<h1>Bonjour</h1><p>Queue persistante</p>",
                "destinataire@exemple.com"
        );
        mailService.submitMail(msg);
        LOG.debug("sendTestMail done for msg = " + msg);
    }

    public int getPendingMails() {
        return mailService.getPendingCount();
    }
    
    public void refresh() {
    // Rien à faire : JSF relit pendingMails
    // Méthode volontairement vide
}
}

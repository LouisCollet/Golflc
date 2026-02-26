/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package mail3;

import static interfaces.Log.LOG;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.Serializable;

//import mail3.MailService;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;

@Named
@ViewScoped
public class MailController implements Serializable {

    @Inject
    private MailService mailService;

    private String email;
    private String subject;
    private String body;

    public void sendMail() {
        LOG.debug("entering sendMail");
        mailService.sendMailAsync(email, subject, body);
        LOG.debug("exiting sendMail");
    }

    // appelé par <p:poll> toutes les X secondes
    public void checkMailStatus() {
        LOG.debug("checking mail status");
     //   if (mailService.isMailSent()) {
     //       FacesContext.getCurrentInstance().addMessage(null,
     //               new FacesMessage(FacesMessage.SEVERITY_INFO,
     //                       "Mail envoyé", "Le mail a été traité avec succès"));
     //       mailService.resetMailFlag();
     //   }
    }

    // getters & setters
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }
    public String getBody() { return body; }
    public void setBody(String body) { this.body = body; }
}

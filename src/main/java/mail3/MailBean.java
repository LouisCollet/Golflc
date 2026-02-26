
package mail3;


import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import mail3.MailService;

//@Named
@RequestScoped
public class MailBean {

    @Inject
    private MailService mailService;

    private String to;
    private String subject;
    private String body;

    public void sendMail() {
        mailService.sendMailAsync(to, subject, body);
    }

    // Getters / Setters
    public String getTo() { return to; }
    public void setTo(String to) { this.to = to; }

    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }

    public String getBody() { return body; }
    public void setBody(String body) { this.body = body; }
}

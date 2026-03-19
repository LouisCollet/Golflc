
package mail3;

import static interfaces.Log.LOG;
import jakarta.ejb.Asynchronous;
import jakarta.ejb.Stateless;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Named;
import jakarta.mail.*;
import jakarta.mail.internet.*;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicBoolean;

@Named
@ApplicationScoped
public class MailService {

    public void sendMailAsync(String to, String subject, String body) {
        new Thread(() -> {
            try {
                // Configuration SMTP (exemple Gmail)
                Properties props = new Properties();
                props.put("mail.smtp.auth", "true");
                props.put("mail.smtp.starttls.enable", "true");
                props.put("mail.smtp.host", "smtp.gmail.com");
                props.put("mail.smtp.port", "587");

                Session session = Session.getInstance(props, new Authenticator() {
                    @Override
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(System.getenv("SMTP_USERNAME"), System.getenv("SMTP_PASSWORD"));
                    }
                });

                Message message = new MimeMessage(session);
                message.setFrom(new InternetAddress("monemail@gmail.com"));
                message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
                message.setSubject(subject);
                message.setText(body);

                Transport.send(message);

                // Notification temps réel côté JSF
                MailWebSocket.sendMailNotification("Mail envoyé à " + to);

            } catch (Exception e) {
                e.printStackTrace();
                MailWebSocket.sendMailNotification("Erreur lors de l'envoi du mail à " + to);
            }
        }).start();
    }
} // end class

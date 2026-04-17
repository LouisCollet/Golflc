package mail;

import jakarta.activation.DataHandler;
import jakarta.activation.DataSource;
import jakarta.activation.FileDataSource;
import jakarta.annotation.Resource;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.concurrent.ManagedExecutorService;
import jakarta.inject.Inject;
import jakarta.mail.Address;
import jakarta.mail.Authenticator;
import jakarta.mail.BodyPart;
import jakarta.mail.Message;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;
import jakarta.mail.util.ByteArrayDataSource;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import static exceptions.LCException.handleGenericException;
import static interfaces.Log.LOG;
import org.apache.logging.log4j.ThreadContext;
import static utils.LCUtil.showMessageFatal;
import static utils.LCUtil.showMessageInfo;

/**
 * Service d'envoi d'emails HTML.
 *
 * Coalescing interne : les appels successifs à sendHtmlMailAsync sont mis en
 * queue. Un seul worker se déclenche, attend 300 ms pour laisser d'autres
 * messages arriver, puis vide la queue et envoie tout sur un seul Transport
 * SMTP. L'appelant n'a rien à regrouper.
 */
@ApplicationScoped
public class MailSender implements Serializable {

    private static final long   serialVersionUID  = 1L;
    private static final String MAILSERVER        = "relay.proximus.be";
    private static final int    COALESCE_DELAY_MS = 300;

    @Inject private entite.Settings settings;

    @Resource
    private ManagedExecutorService mailExecutor;

    // Queue interne — transient car @ApplicationScoped n'est pas passivé
    private transient final LinkedBlockingQueue<MailMessage> mailQueue    = new LinkedBlockingQueue<>();
    private transient final AtomicBoolean                   workerActive = new AtomicBoolean(false);

    public MailSender() { }

    // ========================================
    // MailMessage — paramètres d'un email
    // ========================================

    public record MailMessage(
            String title,
            String text,
            String recipient,
            byte[] pathICS,
            byte[] pathQRC,
            String targetLanguage) { }

    // ========================================
    // ASYNC — enfile et déclenche le worker
    // ========================================

    public CompletableFuture<Void> sendHtmlMailAsync(
            String title,
            String content,
            String recipient,
            byte[] pathICS,
            byte[] pathQRC,
            String targetLanguage) {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        LOG.debug("enqueuing mail to={}", recipient);
        mailQueue.offer(new MailMessage(title, content, recipient, pathICS, pathQRC, targetLanguage));
        scheduleFlush();
        return CompletableFuture.completedFuture(null);
    } // end method

    public CompletableFuture<Void> sendHtmlMailAsync(
            String title,
            String content,
            String recipient,
            byte[] pathICS,
            String targetLanguage) {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        return sendHtmlMailAsync(title, content, recipient, pathICS, null, targetLanguage);
    } // end method

    // ========================================
    // SYNC — envoi immédiat (hors coalescing)
    // ========================================

    public boolean sendHtmlMail(
            String title,
            String content,
            String recipient,
            byte[] pathICS,
            String targetLanguage) {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        return sendHtmlMail(title, content, recipient, pathICS, null, targetLanguage);
    } // end method

    public boolean sendHtmlMail(
            String title,
            String text,
            String recipient,
            byte[] pathICS,
            byte[] pathQRC,
            String targetLanguage) {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        LOG.debug("recipient={}", recipient);
        try {
            sendBatch(List.of(new MailMessage(title, text, recipient, pathICS, pathQRC, targetLanguage)));
            return true;
        } catch (Exception e) {
            handleGenericException(e, methodName);
            return false;
        }
    } // end method

    // ========================================
    // COALESCING — worker interne
    // ========================================

    private void scheduleFlush() {
        if (workerActive.compareAndSet(false, true)) {
            CompletableFuture.runAsync(this::flushQueue, mailExecutor);
        }
    } // end method

    private void flushQueue() {
        try {
            Thread.sleep(COALESCE_DELAY_MS);
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
        }
        List<MailMessage> batch = new ArrayList<>();
        mailQueue.drainTo(batch);
        if (!batch.isEmpty()) {
            LOG.debug("flushing {} mail(s) over one Transport", batch.size());
            try {
                sendBatch(batch);
            } catch (Exception e) {
                LOG.error("flushQueue: failed to send batch of {} mail(s): {}", batch.size(), e.getMessage(), e);
            }
        }
        workerActive.set(false);
        // Si de nouveaux messages sont arrivés pendant l'envoi, relancer
        if (!mailQueue.isEmpty()) {
            scheduleFlush();
        }
    } // end method

    // ========================================
    // ENVOI — un seul Transport pour la batch
    // ========================================

    private void sendBatch(List<MailMessage> mails) throws Exception {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {} count={}", methodName, mails.size());
        final String username = settings.getProperty("SMTP_USERNAME");
        final String password = settings.getProperty("SMTP_PASSWORD");
        Session session = buildSession(username, password);

        long startNanos = System.nanoTime();
        try (Transport transport = session.getTransport("smtp")) {
            transport.connect(MAILSERVER, username, password);
            for (MailMessage mail : mails) {
                MimeMessage msg = buildMimeMessage(session, mail, username);
                if (msg != null) {
                    msg.saveChanges();
                    transport.sendMessage(msg, msg.getAllRecipients());
                    LOG.info("mail sent to {}", mail.recipient()); 
                    // Ajouter infos dans le JSON
                    ThreadContext.put("recipient", mail.recipient());
                    ThreadContext.put("subject", mail.title());
                    LOG.info("mail sent");
                    // Nettoyer ThreadContext pour éviter les fuites
                    ThreadContext.clearAll();
                    
                    
                    
                }
            }
        }
        double elapsedMillis = (System.nanoTime() - startNanos) / 1_000_000.0;
        LOG.debug("batch of {} mail(s) sent in {} ms", mails.size(), elapsedMillis);

        if (jakarta.faces.context.FacesContext.getCurrentInstance() != null) {
            showMessageInfo(mails.size() + " mail(s) sent in " + elapsedMillis + " ms");
        }
    } // end method

    // ========================================
    // HELPERS PRIVÉS
    // ========================================

    private Session buildSession(String username, String password) {
        Properties props = buildProperties();
        Authenticator auth = new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, password);
            }
        };
        Session session = Session.getInstance(props, auth);
        session.setDebug(false);
        return session;
    } // end method

    private MimeMessage buildMimeMessage(Session session, MailMessage mail, String username)
            throws Exception {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);

        String title = mail.title();
        String text  = mail.text();

        if (!"fr".equalsIgnoreCase(mail.targetLanguage())) {
            title = translation.FileTranslation.translateList(List.of(title), mail.targetLanguage());
            text  = translation.FileTranslation.translateList(List.of(text),  mail.targetLanguage());
        }

        if (title != null && (title.contains("\r") || title.contains("\n"))) {
            LOG.error("CRLF injection attempt in title");
            showMessageFatal("Invalid email subject");
            return null;
        }
        if (mail.recipient() != null && (mail.recipient().contains("\r") || mail.recipient().contains("\n"))) {
            LOG.error("CRLF injection attempt in recipient");
            showMessageFatal("Invalid email recipient");
            return null;
        }

        MimeMessage msg = new MimeMessage(session);
        msg.setFrom(new InternetAddress(username, "Application GolfLC"));
        msg.setRecipients(Message.RecipientType.TO, InternetAddress.parse(mail.recipient(), false));

        MimeMultipart multipart = new MimeMultipart("related");
        MimeBodyPart  bodyPart  = new MimeBodyPart();
        String cid = UUID.randomUUID().toString();

        bodyPart.setHeader("MIME-Version", "1.0");
        text = text + "<html><div><img src=\"cid:" + cid + "\" width=\"20%\" height=\"20%\" /></div></html>";
        bodyPart.setContent(text, "text/html; charset=utf-8");
        multipart.addBodyPart(bodyPart);

        MimeBodyPart imagePart  = new MimeBodyPart();
        DataSource   dataSource = new FileDataSource(
                settings.getProperty("IMAGES_LIBRARY") + "golf man drive.jpg");
        imagePart.setDataHandler(new DataHandler(dataSource));
        imagePart.setHeader("Content-ID", "<image>");
        imagePart.setContentID("<" + cid + ">");
        imagePart.setDisposition(MimeBodyPart.INLINE);
        multipart.addBodyPart(imagePart);

        if (mail.pathICS() != null) {
            LOG.debug("attaching ICS");
            MimeBodyPart part   = new MimeBodyPart();
            DataSource   source = new ByteArrayDataSource(mail.pathICS(), "text/calendar");
            part.setDataHandler(new DataHandler(source));
            part.setFileName("Here is your appointment.ics");
            multipart.addBodyPart(part);
        }

        if (mail.pathQRC() != null) {
            LOG.debug("attaching QRCode");
            DataSource source = new ByteArrayDataSource(mail.pathQRC(), "image/png");
            BodyPart   part   = new MimeBodyPart();
            part.setDataHandler(new DataHandler(source));
            part.setDisposition(MimeBodyPart.ATTACHMENT);
            part.setFileName("Here is your QRCode");
            multipart.addBodyPart(part);
        }

        msg.setContent(multipart);
        msg.setHeader("MIME-Version",               "1.0");
        msg.setHeader("X-Mailer",                   "GolfLC Custom Mailer");
        msg.setHeader("Precedence",                 "bulk");
        msg.setHeader("Auto-Submitted",             "auto-generated");
        msg.setHeader("Content-Transfer-Encoding",  "base64");
        msg.setSubject(title, "utf-8");
        msg.setSentDate(new Date());

        return msg;
    } // end method

    private Properties buildProperties() {
        Properties props = new Properties();
        props.put("mail.password",              settings.getProperty("SMTP_PASSWORD"));
        props.put("mail.smtp.host",             MAILSERVER);
        props.put("mail.user",                  settings.getProperty("SMTP_USERNAME"));
        props.put("mail.smtp.from",             settings.getProperty("SMTP_USERNAME"));
        props.put("mail.smtp.port",             "587");
        props.put("mail.smtp.debug",            "false");
        props.put("mail.debug.auth",            "true");
        props.put("java.security.debug",        "false");
        props.put("mail.smtp.auth",             "true");
        props.put("mail.smtp.starttls.enable",  "true");
        props.put("mail.smtp.auth.mechanisms",  "LOGIN");
        props.put("mail.smtp.ssl.enable",       "false");
        props.put("mail.ssl.enable",            "false");
        return props;
    } // end method

    /*
    void main() {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        try {
            String content = "Ceci est le texte du mail <b> gras </b>";
            String title   = "Ceci est le sujet du mail";
            String to      = System.getenv("SMTP_USERNAME");
            boolean b = sendHtmlMail(title, content, to, null, "fr");
            LOG.debug("result={}", b);
        } catch (Exception e) {
            handleGenericException(e, methodName);
        }
    } // end main
    */

} // end class

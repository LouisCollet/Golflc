package mail;

import jakarta.activation.DataHandler;
import jakarta.activation.DataSource;
import jakarta.activation.FileDataSource;
import jakarta.annotation.Resource;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.concurrent.ManagedExecutorService;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.mail.Address;
import jakarta.mail.Authenticator;
import jakarta.mail.BodyPart;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;
import jakarta.mail.util.ByteArrayDataSource;
import manager.PlayerManager;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Date;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.atomic.AtomicBoolean;

import static exceptions.LCException.handleGenericException;
import static interfaces.Log.LOG;
import static utils.LCUtil.showDialogInfo;
import static utils.LCUtil.showMessageFatal;
import static utils.LCUtil.showMessageInfo;

/**
 * Service d'envoi d'emails HTML
 * ✅ @ApplicationScoped — singleton CDI
 * ✅ @Asynchronous supprimé — incompatible avec @ApplicationScoped CDI
 * ✅ ManagedExecutorService pour async
 * ✅ Settings injecté — plus de chemins hard-coded
 * ✅ buildProperties() — méthode d'instance (plus static)
 * ✅ Standards CDI : methodName + handleGenericException
 */
@Named("sendEmail")
@ApplicationScoped
public class MailSender implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject private PlayerManager       playerManager;
    @Inject private utils.QRCodeService qrService;
    @Inject private entite.Settings     settings;

    // ✅ ManagedExecutorService — fourni par WildFly pour l'async CDI
    @Resource
    private ManagedExecutorService mailExecutor;

    private final AtomicBoolean mailSent  = new AtomicBoolean(false);
    private volatile Session    session   = null;

    // ========================================
    // ASYNC — envoi asynchrone
    // ✅ CompletableFuture via ManagedExecutorService (pas @Asynchronous EJB)
    // ========================================

    public CompletableFuture<Void> sendHtmlMailAsync(
            String title,
            String content,
            String recipient,
            byte[] pathICS,
            byte[] pathQRC,
            String targetLanguage) {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName + " - recipient = " + recipient);

        return CompletableFuture.runAsync(() -> {
            try {
                LOG.info(methodName + " - sending mail to {}", recipient);
                sendHtmlMail(title, content, recipient, pathICS, pathQRC, targetLanguage);
                mailSent.set(true);
                LOG.info(methodName + " - mail sent successfully to {}", recipient);
            } catch (Exception e) {
                mailSent.set(false);
                LOG.error(methodName + " - mail sending failed to {}", recipient, e);
                throw new CompletionException(e);
            }
        }, mailExecutor);
    } // end method

    // ========================================
    // SEND — surcharge sans QRCode
    // ========================================

    public boolean sendHtmlMail(
            String title,
            String content,
            String recipient,
            byte[] pathICS,
            String targetLanguage) {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName + " - without pathQRC");
        return sendHtmlMail(title, content, recipient, pathICS, null, targetLanguage);
    } // end method

    // ========================================
    // SEND ASYNC — surcharge sans QRCode
    // ========================================

    public CompletableFuture<Void> sendHtmlMailAsync(
            String title,
            String content,
            String recipient,
            byte[] pathICS,
            String targetLanguage) {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName + " - async without pathQRC");
        return sendHtmlMailAsync(title, content, recipient, pathICS, null, targetLanguage);
    } // end method

    // ========================================
    // SEND — méthode principale
    // ========================================

    public boolean sendHtmlMail(
            String title,
            String text,
            String recipient,
            byte[] pathICS,
            byte[] pathQRC,
            String targetLanguage) {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName + " - recipient = " + recipient);
        try {
            if (pathICS != null) LOG.debug(methodName + " - pathICS present");
            if (pathQRC != null) LOG.debug(methodName + " - pathQRC present");

            // ✅ Traduction si nécessaire
            if (!"fr".equalsIgnoreCase(targetLanguage)) {
                title = translation.FileTranslation.translateList(Arrays.asList(title), targetLanguage);
                text  = translation.FileTranslation.translateList(Arrays.asList(text),  targetLanguage);
            }

            final String mailserver = "relay.proximus.be";
            final String username   = settings.getProperty("SMTP_USERNAME");   // ✅ via Settings
            final String password   = System.getenv("SMTP_PASSWORD");

            Properties props = buildProperties();
            LOG.debug(methodName + " - properties built");

            Authenticator auth = new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(username, password);
                }
            };

            session = Session.getInstance(props, auth);
            session.setDebug(false);
            LOG.debug(methodName + " - session created");

            // ✅ Security: sanitize inputs — prevent email header injection
            if (title != null && (title.contains("\r") || title.contains("\n"))) {
                LOG.error(methodName + " - CRLF injection attempt in title");
                showMessageFatal("Invalid email subject");
                return false;
            }
            if (recipient != null && (recipient.contains("\r") || recipient.contains("\n"))) {
                LOG.error(methodName + " - CRLF injection attempt in recipient");
                showMessageFatal("Invalid email recipient");
                return false;
            }

            MimeMessage msg = new MimeMessage(session);
            msg.setFrom(new InternetAddress(username, "Application GolfLC"));
            msg.setRecipients(Message.RecipientType.TO,
                    InternetAddress.parse(recipient, false));

            // ✅ Construction multipart
            MimeMultipart multipart = new MimeMultipart("related");
            MimeBodyPart  bodyPart  = new MimeBodyPart();
            String cid = UUID.randomUUID().toString();

            bodyPart.setHeader("MIME-Version", "1.0");
            text = text + "<html><div><img src=\"cid:" + cid
                    + "\" width=\"20%\" height=\"20%\" /></div></html>";
            bodyPart.setContent(text, "text/html; charset=utf-8");
            multipart.addBodyPart(bodyPart);

            // ✅ Image via Settings — plus de chemin hard-coded
            MimeBodyPart imagePart  = new MimeBodyPart();
            DataSource   dataSource = new FileDataSource(
                    settings.getProperty("IMAGES_LIBRARY") + "golf man drive.jpg");
            LOG.debug(methodName + " - dataSource = " + dataSource);
            imagePart.setDataHandler(new DataHandler(dataSource));
            imagePart.setHeader("Content-ID", "<image>");
            imagePart.setContentID("<" + cid + ">");
            imagePart.setDisposition(MimeBodyPart.INLINE);
            multipart.addBodyPart(imagePart);

            // ✅ Pièce jointe ICS
            if (pathICS != null) {
                LOG.debug(methodName + " - attaching ICS");
                MimeBodyPart attachmentPart = new MimeBodyPart();
                DataSource source = new ByteArrayDataSource(pathICS, "text/calendar");
                attachmentPart.setDataHandler(new DataHandler(source));
                attachmentPart.setFileName("Here is your appointment.ics");
                multipart.addBodyPart(attachmentPart);
                LOG.debug(methodName + " - ICS attached");
            }

            // ✅ Pièce jointe QRCode
            if (pathQRC != null) {
                LOG.debug(methodName + " - attaching QRCode");
                DataSource source          = new ByteArrayDataSource(pathQRC, "image/png");
                BodyPart   messageBodyPart = new MimeBodyPart();
                messageBodyPart.setDataHandler(new DataHandler(source));
                messageBodyPart.setDisposition(MimeBodyPart.ATTACHMENT);
                messageBodyPart.setFileName("Here is your QRCode");
                multipart.addBodyPart(messageBodyPart);
                LOG.debug(methodName + " - QRCode attached");
            }

            // ✅ Headers
            msg.setContent(multipart);
            msg.setHeader("MIME-Version",               "1.0");
            msg.setHeader("X-Mailer",                   "GolfLC Custom Mailer");
            msg.setHeader("Precedence",                 "bulk");
            msg.setHeader("Auto-Submitted",             "auto-generated");
            msg.setHeader("Content-Transfer-Encoding",  "base64");
            msg.setSubject(title, "utf-8");
            msg.setSentDate(new Date());

            // ✅ Envoi avec try-with-resources
            long startNanos = System.nanoTime();
            try (Transport transport = session.getTransport("smtp")) {
                transport.connect(mailserver, username, password);
                msg.saveChanges();
                transport.sendMessage(msg, msg.getAllRecipients());
            }

            long   elapsedNanos  = System.nanoTime() - startNanos;
            double elapsedMillis = elapsedNanos / 1_000_000.0;
            LOG.debug(methodName + " - elapsed = " + elapsedMillis + " ms");

            Address[] recipients = msg.getAllRecipients();
            LOG.info(methodName + " - mail sent to {} recipient(s)",
                    recipients != null ? recipients.length : 0);
            showMessageInfo("Mail sent to " + Arrays.toString(recipients)
                    + " in " + elapsedMillis + " ms");
            showDialogInfo("Mail envoyé à " + Arrays.toString(recipients)
                    + " ms = " + elapsedMillis);

            return true;

        } catch (MessagingException e) {
            handleGenericException(e, methodName);
            return false;
        } catch (Exception e) {
            handleGenericException(e, methodName);
            return false;
        }
    } // end method

    // ========================================
    // BUILD PROPERTIES
    // ✅ Méthode d'instance — plus static
    // ========================================

    public Properties buildProperties() {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName);

        Properties props = new Properties();
        props.put("mail.password",              System.getenv("SMTP_PASSWORD"));
        props.put("mail.smtp.host",             "relay.proximus.be");
        props.put("mail.user",                  System.getenv("SMTP_USERNAME"));
        props.put("mail.smtp.from",             System.getenv("SMTP_USERNAME"));
        props.put("mail.smtp.port",             "587");
        props.put("mail.smtp.debug",            "false");
        props.put("mail.debug.auth",            "true");
        props.put("java.security.debug",        "false");
        props.put("mail.smtp.auth",             "true");
        props.put("mail.smtp.starttls.enable",  "true");
        props.put("mail.smtp.auth.mechanisms",  "LOGIN");
        props.put("mail.smtp.ssl.enable",       "false");
        props.put("mail.ssl.enable",            "false");

        LOG.debug(methodName + " - properties built");
        return props;
    } // end method

    // ========================================
    // HELPERS
    // ========================================

    public boolean isMailSent()  { return mailSent.get(); }
    public void resetMailFlag()  { mailSent.set(false); }

    // ========================================
    // MAIN DE TEST - conservé commenté
    // ========================================

    /*
    void main(String[] args) {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName);
        try {
            String content = "Ceci est le texte du mail <b> gras </b>";
            String title   = "Ceci est le sujet du mail";
            String to      = System.getenv("SMTP_USERNAME");

            Player player = new Player();
            player.setIdplayer(456783);
            player.setPlayerLastName("Muntingh");
            player.setPlayerLanguage("fr");
            player.setPlayerEmail("theo.muntingh@skynet.be");

            Club club = new Club();
            club.setIdclub(108);

            Course course = new Course();
            course.setCourseName("Parcours l'Anglais");

            Round round = new Round();
            round.setRoundDate(LocalDateTime.of(2018, Month.NOVEMBER, 17, 12, 15));

            Player invitedBy = new Player();
            invitedBy.setIdplayer(324713);

            byte[] pathICS = new ical.IcalService().generateIcs(
                    player, invitedBy, round, club, course, true);

            boolean b = sendHtmlMail(title, content, to, pathICS, "es");
            LOG.debug(methodName + " - result = " + b);

        } catch (Exception e) {
            handleGenericException(e, methodName);
        }
    } // end main
    */

} // end class
/*
import entite.Club;
import entite.Course;
import entite.Player;
import entite.Round;
import static interfaces.Log.LOG;
import jakarta.activation.DataHandler;
import jakarta.activation.DataSource;
import jakarta.activation.FileDataSource;
import jakarta.activation.UnsupportedDataTypeException;
import jakarta.annotation.Resource;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.mail.Address;
import jakarta.mail.Authenticator;
import jakarta.mail.BodyPart;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;
import jakarta.mail.util.ByteArrayDataSource;
import java.sql.Connection;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.*;
import connection_package.DBConnection;
import static utils.LCUtil.showMessageFatal;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import jakarta.ejb.Asynchronous;
import jakarta.ejb.Stateless;
import jakarta.enterprise.concurrent.ManagedExecutorService;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.atomic.AtomicBoolean;
import manager.PlayerManager;
import static utils.LCUtil.showDialogInfo;
import static utils.LCUtil.showMessageInfo;
@Named("sendEmail") // this qualifier  makes a bean EL-injectable (Expression Language) pour tester via le menu
//@RequestScoped
@ApplicationScoped
public class MailSender {
    @Inject private PlayerManager playerManager;
    @Inject utils.QRCodeService qrService;
    @Inject private entite.Settings settings;        // ✅ injection CDI
    // ExecutorService pour exécuter les emails en arrière-plan
  //  private static final ExecutorService MailExecutor = Executors.newSingleThreadExecutor();
    private final AtomicBoolean mailSent = new AtomicBoolean(false);
    private volatile Transport transport = null;
    private volatile Session session = null;
    
@Resource
private ManagedExecutorService mailExecutor;


//@Retry( from microprofile
//    maxRetries = 3,
//    delay = 2_000,
//    retryOn = MessagingException.class
//)
@Asynchronous
public CompletableFuture<Void> sendHtmlMailAsync(
        String title,
        String content,
        String recipient,
        byte[] pathICS,
        byte[] pathQRC,
        String targetLanguage  // was Locale
) {
    return CompletableFuture.runAsync(() -> {
        try {
            LOG.info("CompletableFuture.runAsync going to sendHtmlMail Sending mail to {}", recipient);
            sendHtmlMail(title, content, recipient, pathICS, pathQRC, targetLanguage);
            LOG.info("Mail sent successfully to {}", recipient);
        } catch (Exception e) {
            LOG.error("Mail sending failed to {}", recipient, e);
            throw new CompletionException(e);
        }
    }, mailExecutor);
}





/*
dans error 
if (attempt >= maxRetries) {
                    LOG.error("Échec définitif après {} tentatives pour {}", attempt, recipient, e);
                    result.completeExceptionally(e);
                } else {
                    LOG.info("Replanification de la tentative {} pour {} dans {} ms",
                            attempt + 1, recipient, retryDelay.toMillis());
                    mailExecutor.schedule(
                            () -> attemptSend(result, title, content, recipient,
                                    pathICS, pathQRC, targetLanguage,
                                    attempt + 1, maxRetries, retryDelay, timeout),
                            retryDelay.toMillis(),
                            TimeUnit.MILLISECONDS
                    )




 public boolean sendHtmlMailAsync(final String title,
        final String content,
        final String recipient,
     //   final Path pathICS, // calendar
        byte[] pathICS, // calendar
        final byte[] pathQRC, // mod 28-12-2025
        final String targetLanguage) throws Exception{ 
      LOG.debug("entering sendHtmlMailAsync");
     CompletableFuture.runAsync(() -> {
            try {
                LOG.debug("going to sendHtmlMail from CompletableFuture.runAsync");
                 sendHtmlMail(title, content, recipient, pathICS, pathQRC, targetLanguage);
                mailSent.set(true);
            } catch (Exception e) {
                mailSent.set(false);
            }
        });
     
     LOG.debug("after completable future in  sendHtmlMailAsync");
     LOG.debug(" before mailExecutor");
        MailExecutor.submit(() -> {
            try {
                LOG.debug("going to sendHtmlMail from MailExecutor");
                sendHtmlMail(title, content, recipient, pathICS, pathQRC, targetLanguage);

            } catch (Exception e) {
                LOG.error("Erreur lors de l'envoi de l'email", e);

            }
        });
        LOG.debug(" after MailExecutor");
        return true;
    } // end method
    
 
 
    public boolean isMailSent() {
        return mailSent.get();
    }

    public void resetMailFlag() {
        mailSent.set(false);
    }
    
    
    public boolean sendHtmlMail(final String title,
        final String content,
        final String recipient,
     //   final Path pathICS, // calendar
        byte[] pathICS, // calendar
    //    Path pathQRC, // QR Code
    //    final byte[] pathQRC, // mod 28-12-2025
        final String targetLanguage) throws Exception{
        
           LOG.debug(" starting SendEmail sendHtmlMail without pathQRC ");
      return sendHtmlMail(title,
            content,
            recipient,
            pathICS, // calendar
            null, // byte[] pathQRC,QR Code// mod 28-12-2025
            targetLanguage);
    
    } // end method
@Asynchronous // ne change rien !
public boolean sendHtmlMail(String title,
        String text,
        final String recipient,
       // Path pathICS, // calendar
        byte[] pathICS, // calendar
    //    Path pathQRC, // QR Code
        byte[] pathQRC, // mod 28-12-2025
        String targetLanguage) throws UnsupportedDataTypeException, Exception{
           LOG.debug(" entering mail.MailSender.SendHtmlMail Email sendHtmlMail ");
        if(pathICS != null){
           LOG.debug("entering SendEmail.sendHtmlMail with pathICS = " + pathICS.toString()); //.getFileName());
        }
        if(pathQRC != null){
           LOG.debug("entering SendEmail.sendHtmlMail with pathQRC = " + pathQRC.toString()); //.getRoot());
           
        }
 try{
 // translations - pas de traduction pour les textes en français
  if( ! "fr".equalsIgnoreCase(targetLanguage)){
     title = translation.FileTranslation.translateList(Arrays.asList(title), targetLanguage);
   //    LOG.debug("translated title = " + title);
     text = translation.FileTranslation.translateList(Arrays.asList(text), targetLanguage);
   //    LOG.debug("translated text = " + text);
  }
 // end translations   
 
    final String mailserver = "relay.proximus.be";
    // https://blogs.oracle.com/apanicker/entry/java_code_for_smtp_server
   // final String username = System.getenv("SMTP_USERNAME");
    final String username = settings.getProperty("SMTP_USERNAME"); // mod 31-12-2025
    Properties props = buildProperties();  //standard properties
    // vérifier si Settings accessible ! non pour RUN
      LOG.debug("after props");
    Authenticator auth = new Authenticator() {
       @Override
       protected PasswordAuthentication getPasswordAuthentication() { 
      // return new PasswordAuthentication(username, System.getenv("SMTP_PASSWORD")); 
       return new PasswordAuthentication(username, System.getenv("SMTP_PASSWORD")); 
// first = username
            }
        };
 //   LOG.debug("auth = " + auth);
     // creates a new session with an authenticator
   // Session session = Session.getInstance(props, auth); // crash !!
     session = Session.getInstance(props, auth);
     LOG.debug("after Session.getInstance");
    
  //     LOG.debug("line 03 - session build = !" + mailSession);
    session.setDebug(false); // // mettre aussi mail.smtp.debug(true); voir plus haut, debug dans console only
        LOG.debug("this is a NON debug mail sending !");
 //  Create a new message --        
    MimeMessage msg = new MimeMessage(session);
    // -- Set the FROM and TO fields --
    
    msg.setFrom(new InternetAddress(username, "Application GolfLC"));
    msg.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipient, false));

//http://blog.smartbear.com/how-to/how-to-send-email-with-embedded-images-using-java/
//http://stackoverflow.com/questions/3902455/mail-multipart-alternative-vs-multipart-mixed
 //Lastly, we also tell the mail client that the text part and the image part are related
// and should be shown as a single item, not as separate pieces of the message.
// We do so by changing how we create the message content:MimeMultipart content = new MimeMultipart("related");
 //   MimeMultipart content = new MimeMultipart("alternative");
 
 // envoi du texte et de l'image
    MimeMultipart multipart = new MimeMultipart("related"); 
    MimeBodyPart bodyPart = new MimeBodyPart();
    
    // ContentID is used by both parts
    String cid = UUID.randomUUID().toString();
//MIME -- Multipurpose Internet Mail Extensions (MIME) is an Internet Standard for the format of e-mail.
//Virtually all Internet e-mail is transmitted via SMTP in MIME format.
//Internet e-mail is so closely associated with the SMTP and MIME standards
//that it is sometimes called SMTP/MIME e-mail.
    bodyPart.setHeader("MIME-Version","1.0" ); 
 //       LOG.debug(" SendEmail sendHtmlMail texte 1 = " + texte);
 
 //   text= text + "<html><div>And <b>here</b>'s an image: <img src=\"cid:" + cid + "\"></div></html>";
    // on ajoute une image au texte
    //
    text = text + "<html><div><img src=\"cid:" + cid + "\" width=\"20%\" height=\"20%\" /></div></html>" ;
    bodyPart.setContent(text,"text/html; charset=utf-8");
    multipart.addBodyPart(bodyPart);
 // Image part 
    MimeBodyPart imagePart = new MimeBodyPart();
  //  DataSource dataSource = new FileDataSource(Settings.getProperty("IMAGES_LIBRARY") + "golf man drive.jpg");
    DataSource dataSource = new FileDataSource("c:/log/golf man drive.jpg"); // à améliorer hard coded !
        LOG.debug("Datasource = " + dataSource.toString());
    imagePart.setDataHandler(new DataHandler(dataSource));
    imagePart.setHeader("Content-ID", "<image>");
    imagePart.setContentID("<" + cid + ">");
  //  Notice how we tell mail clients that the image is to be displayed inline
  // (not as an attachment)
    imagePart.setDisposition(MimeBodyPart.INLINE);
    multipart.addBodyPart(imagePart);
  // voir aussi : https://www.tabnine.com/web/assistant/code/rs/5c65ac271095a500016ea984#L260
  // methodes spécialisée !!
  //  Calendar File attachment
   if(pathICS != null){
          LOG.debug("starting ics with pathICS = " + pathICS);
       MimeBodyPart attachmentPart = new MimeBodyPart();
  // old     DataSource source = new FileDataSource(pathICS.toFile());
      // DataSource source = new ByteArrayDataSource(pathQRC, "image/png");
       DataSource source = new ByteArrayDataSource(pathICS,"image/png"); // mod 31-12-2025
       LOG.debug("line 10");
       attachmentPart.setDataHandler(new DataHandler(source));
       attachmentPart.setFileName("Here is your appointment.ics"); // si omis , alors "part 1.2"
       multipart.addBodyPart(attachmentPart);
          LOG.debug("ending ics");
  }
   /*
      if(pathICS != null){
          LOG.debug("starting vcf with pathICS = " + pathICS);
       MimeBodyPart attachmentPart = new MimeBodyPart();
       DataSource source = new FileDataSource(pathICS.toFile());
       attachmentPart.setDataHandler(new DataHandler(source));
       attachmentPart.setFileName("Here is your vCard.vcf"); // si omis , alors "part 1.2"
       multipart.addBodyPart(attachmentPart);
          LOG.debug("ending vcf");
  }
   
  // QRCode attachment ?
   if(pathQRC != null){
         LOG.debug("starting QRCode with pathqRC = " + pathQRC);
   // old was file    DataSource source = new FileDataSource(pathQRC.toFile());
       
       DataSource source = new ByteArrayDataSource(pathQRC, "image/png");
       BodyPart messageBodyPart = new MimeBodyPart();
       messageBodyPart.setDataHandler(new DataHandler(source));
       messageBodyPart.setDisposition(MimeBodyPart.ATTACHMENT);
       messageBodyPart.setDescription("this is the description, lC"); // utilité ?
       messageBodyPart.setFileName("Here is your QRCode");
       multipart.addBodyPart(messageBodyPart);
         LOG.debug("ending QRCode");
} 
   
    msg.setContent(multipart);
    msg.setHeader("MIME-Version" ,"1.0");
    msg.setHeader("X-Mailer", "GolfLC Custom Mailer");
    msg.setHeader("Precedence", "bulk"); // 26/12/2016
    msg.setHeader("Auto-Submitted", "auto-generated"); // 26/12/2016
    msg.setHeader("Content-Transfer-Encoding", "base64");
    msg.setSubject(title,"utf-8"); // new 05-10-2021
    msg.setSentDate(new Date());
     LOG.debug("before transport");
     
 //send the email message
    long startNanos = System.nanoTime();
    try (Transport transport = session.getTransport("smtp")) {
         transport.connect(mailserver, username, System.getenv("SMTP_PASSWORD")); // mod 28-12-2025
         msg.saveChanges();
         transport.sendMessage(msg, msg.getAllRecipients() );
    } //end try
    LOG.debug("after transport");
    long elapsedNanos = System.nanoTime() - startNanos;
      LOG.debug("Elapsed time Nanos: " + elapsedNanos);
    double elapsedMillis = elapsedNanos / 1_000_000.0;
       LOG.debug("Elapsed time Millis: " + elapsedMillis + " ms");
    
    Address[] recipients = msg.getAllRecipients();
         LOG.info("SMTP mail sent successfully to {} recipient(s)", recipients != null ? recipients.length : 0);
         showMessageInfo("SMTP mail sent successfully to {} recipient(s)" + Arrays.toString(recipients) + " ms = " + elapsedMillis);
         showDialogInfo("from MailSender : Mail envoyé avec succès à " + Arrays.toString(recipients) + " ms = " + elapsedMillis);
                // Output now correct.

//    LOG.debug( "HTML = text/html : " + htmlPart.isMimeType( "text/html" ) );
//    LOG.debug( "HTML Content Type: " + htmlPart.getContentType() );
//   LOG.debug( "HTML Data Handler: " + htmlPart.getDataHandler().getContentType() );
//   LOG.debug( "Image Data Handler: " + imagePart.getDataHandler().getContentType() );

    return true;
}catch (MessagingException e){
    
          String msg = "MessagingException in sendHtmlMail = " + e;
            LOG.error(msg);
            showMessageFatal(msg);
            throw e;
         //   return false;
}catch (Exception e){
            String msg = "Exception in sendHtmlMail = " + e;
            LOG.error(msg);
            showMessageFatal(msg);
            return false;
}
} // end method sendHtmlMail

public static Properties buildProperties() {
    //https://smartbear.com/blog/how-to-send-email-with-embedded-images-using-java/
       LOG.debug("entering buildProperties");
  //  Properties props = System.getProperties();
    Properties props = new Properties();  // new 31-12-2025
  //  props.put("mail.password", System.getenv("SMTP_PASSWORD")); // mod 28-12-2025
    props.put("mail.password", System.getenv("SMTP_PASSWORD")); // mod 31-12-2025
        LOG.debug("after Settings SMTP_PASSWORD line 02");
    props.put("mail.smtp.host", "relay.proximus.be");
    props.put("mail.user", System.getenv("SMTP_USERNAME"));
    props.put("mail.smtp.from", System.getenv("SMTP_USERNAME"));
    props.put("mail.smtp.port", "587");
    // set to false on 05-08-2018
    props.put("mail.smtp.debug", "false"); // mettre aussi session.setDebug(true); voir plus loin, debug dans console only
    props.put("mail.debug.auth", "true"); // new 24/02/2017 was "true"
    props.put("java.security.debug", "false"); // new 26/12/2016  // was true
    
 //   props.put("mail.smtp.timeout", 15000); // default infinite
 //https://javaee.github.io/javamail/docs/api/com/sun/mail/smtp/package-summary.html   
 //   props.put("mail.smtp.connectiontimeout", 15000);// mod 22-11-2021 default infinite
 //   props.put("mail.smtp.writetimeout", 15000);// mod 22-11-2021 default infinite
 //   LOG.debug("line 11");
/// deleted 28-12-2025    Settings.init();
  //     LOG.debug("settings pasw = " + Settings.getMAIL());
    props.put("mail.smtp.auth", "true");
    // If true, attempt to authenticate the user using the AUTH command. Defaults to false.
    props.put("mail.smtp.starttls.enable","true"); // mod 09/04/2018
    //If true, enables the use of the STARTTLS command (if supported by the server)
    //to switch the connection to a TLS-protected connection before issuing any login commands.
    //Note that an appropriate trust store must configured so that the client will trust
    //the server's certificate. Defaults to false. 
    props.put("mail.smtp.auth.mechanisms","LOGIN");
    //If set, lists the authentication mechanisms to consider, and the order in which to consider them.
    //Only mechanisms supported by the server and supported by the current implementation will be used. 
    //The default is "LOGIN PLAIN DIGEST-MD5 NTLM", which includes all the authentication mechanisms 
    //supported by the current implementation except XOAUTH2. 
    //    props.put("mail.imap.auth.ntlm.domain","");  // new 26/12/2016
    // 24/02/2017 essai de mail crypté props.put("mail.smtp.ssl.enable", "true");
  //  props.put("mail.smtp.ssl.enable", "true"); // mod 09/04/2018
    props.put("mail.smtp.ssl.enable", "false"); // mod 09/04/2018
    props.put("mail.ssl.enable", "false"); // false = working with normal mail
      LOG.debug("exiting buildProperties");
    return props;
  } //end method

void main(String[] args) throws Exception{ // for testing purposes
//    LOG.debug("args = " + Arrays.toString(args));
   Connection conn = new DBConnection().getConnection();
  try{
   long start = System.nanoTime();
    String content = "Ceci est le texte du mail <b> gras </b>"
        + "</br> really new line ?"
        + "</br> now italic : " 
        + " </br> now <i> italiques </i>";
   String title = "Ceci est le sujet du mail, louis";
   String to = System.getenv("SMTP_USERNAME") + "," + System.getenv("SMTP_USERNAME_ONDUTY");
   // a faire : envoi QRC
   
   //byte[] pathQRC = qrService.generateQR(content, 200);   because "this.qrService" is null
   //          LOG.debug("reponse de qrService = " + pathQRC.toString()); 
             
   // à faire : envoi 
      Player player = new Player();
      player.setIdplayer(456783);  // muntingh
      player.setPlayerLastName("Muntingh");
      player.setPlayerLanguage("fr");
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
      club.setIdclub(108);  //rigenée
      club = new read.ReadClub().read(club);
      Course course = new Course();
      course.setCourseName("Parcours l'Anglais");
      Round round = new Round(); 
      round.setRoundDate(LocalDateTime.of(2018, Month.NOVEMBER, 17, 12, 15));
      round.setRoundGame("round game : STABLEFORD");
      round.setPlayersString("inscrits précédemment : Corstjens, Bauer");
      Player p1 = new Player();
      p1.setIdplayer(456784);
   //   p1 = new read.ReadPlayer().read(player, conn);
      p1 = playerManager.readPlayer(player.getIdplayer());
      Player p2 = new Player();
      p2.setIdplayer(456785);
    //  p2 = new read.ReadPlayer().read(player, conn);
   //   p2 = playerManager.readPlayer(player.getIdplayer());
      p2 = playerManager.readPlayer(player.getIdplayer());
      round.setPlayers(List.of(p1,p2));
      byte[] pathICS = new ical.IcalService().generateIcs(player, invitedBy, round, club, course, true);
   //byte[] pathQRC = null;
   // pas de ics
 //   for(int i = 0; i < args.length; i++) {
 //           LOG.debug("Argument " + i + " /" +args[i]);
    boolean b = new mail.MailSender().sendHtmlMail(title, content, to, pathICS, "es");
      LOG.debug("boolean result = " + b);
    long elapsedNanos = System.nanoTime() - start;
      LOG.debug("Elapsed time Nanos: " + elapsedNanos);
    double elapsedMillis = elapsedNanos / 1_000_000.0;
       LOG.debug("Elapsed time Millis: " + elapsedMillis + " ms");
   //    LOG.debug("execution time = " + elapsedMillis);
   } catch (Exception e) {
            String msg = "Â£Â£ Exception in main = " + e.getMessage();
            LOG.error(msg);
//            LCUtil.showMessageFatal(msg);
   }finally{
         DBConnection.closeQuietly(conn, null, null , null); 
   }
   
   
} // end main
} // end class
*/
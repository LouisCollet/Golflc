package mail;

import entite.Settings;
import static interfaces.Log.LOG;
import jakarta.activation.DataHandler;
import jakarta.activation.DataSource;
import jakarta.activation.FileDataSource;
import jakarta.activation.UnsupportedDataTypeException;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Named;
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
import java.nio.file.Path;
import java.util.*;
import static utils.LCUtil.showMessageFatal;
@Named("sendEmail") // this qualifier  makes a bean EL-injectable (Expression Language) pour tester via le menu
@RequestScoped
public class SendEmail {

public boolean sendHtmlMail(String title,
        String text,
        final String to,
        Path pathICS, // calendar
        Path pathQRC, // QR Code
        String targetLanguage) throws UnsupportedDataTypeException, Exception{
           LOG.debug(" starting SendEmail sendHtmlMail ");
        if(pathICS != null){
           LOG.debug("entering SendEmail.sendHtmlMail with pathICS = " + pathICS.getFileName());
        }
        if(pathQRC != null){
           LOG.debug("entering SendEmail.sendHtmlMail with pathQRC = " + pathQRC.getRoot());
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
    final String username = "louis.collet@skynet.be";
    Properties props = buildProperties();  //standard properties
    // vérifier si Settings accessible ! non pour RUN
    
    
    
    props.put("mail.password", Settings.getProperty("MAIL")); // mod 28-11-2021
        //LOG.debug("line 01");
    
    Authenticator auth = new Authenticator() {
       @Override
       protected PasswordAuthentication getPasswordAuthentication() {  // was public
       return new PasswordAuthentication(username, Settings.getProperty("MAIL"));  // first = username
            }
        };
 //   LOG.debug("auth = " + auth);
     // creates a new session with an authenticator
    Session mailSession = Session.getInstance(props, auth); // crash !!
  //     LOG.debug("line 03 - session build = !" + mailSession);
    mailSession.setDebug(false); // // mettre aussi mail.smtp.debug(true); voir plus haut, debug dans console only
        LOG.debug("this is a NON debug mail sending !");
 //  Create a new message --        
    MimeMessage msg = new MimeMessage(mailSession);
    // -- Set the FROM and TO fields --
    
    msg.setFrom(new InternetAddress(username, "Application GolfLC"));
    msg.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to, false));

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
    DataSource dataSource = new FileDataSource(Settings.getProperty("IMAGES_LIBRARY") + "golf man drive.jpg");
        LOG.debug("Datasource = " + dataSource);
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
       DataSource source = new FileDataSource(pathICS.toFile());
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
   */
  // QRCode attachment ?
   if(pathQRC != null){
         LOG.debug("starting QRCode with pathqRC = " + pathQRC);
       DataSource source = new FileDataSource(pathQRC.toFile());
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

 //send the email message
    try (Transport transport = mailSession.getTransport("smtp")) {
  //       transport.connect(mailserver, username, Settings.getMAIL());
         transport.connect(mailserver, username, Settings.getProperty("MAIL"));
         msg.saveChanges();
         transport.sendMessage(msg, msg.getAllRecipients() );
    } //end try
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
            return false;
}catch (Exception e){
            String msg = "Exception in sendHtmlMail = " + e;
            LOG.error(msg);
            showMessageFatal(msg);
            return false;
}
} // end method sendHtmlMail

public static Properties buildProperties() {
    //https://smartbear.com/blog/how-to-send-email-with-embedded-images-using-java/
//       LOG.debug("entering buildProperties");
    Properties props = System.getProperties();
    props.put("mail.smtp.host", "relay.proximus.be");
    props.put("mail.user", "louis.collet@skynet.be");
    props.put("mail.smtp.from", "louis.collet@skynet.be");
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
    Settings.init();
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
  }

void main(String[] args) throws Exception{ // for testing purposes
    LOG.debug("args = " + Arrays.toString(args));
    
    String text = "Ceci est le texte du mail <b> gras </b>"
        + "</br> really new line ?"
        + "</br> now italic : " 
        + " </br> now <i> italiques </i>";
   String title = "Ceci est le sujet du mail, louis";
   String to = "louis.collet@skynet.be;louis.collet.onduty@gmail.com";
   Path path = null;
 //   for(int i = 0; i < args.length; i++) {
 //           LOG.debug("Argument " + i + " /" +args[i]);
         boolean b = new mail.SendEmail().sendHtmlMail(title, text, to, path, path, "es"); // args[i]);
} // end main
} // end class
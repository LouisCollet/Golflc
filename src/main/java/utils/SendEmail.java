package utils;

import java.io.*;
import java.util.*;
import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.activation.UnsupportedDataTypeException;
import javax.mail.*;
import javax.mail.internet.*;
import javax.mail.internet.InternetAddress;
import lc.golfnew.Constants;

public class SendEmail implements interfaces.Log{
    private String text; // = msgText;
    private static String fromEmail;
    private File attachment;

public boolean sendHtmlMail(final String sujet, String texte, final String to, String type) throws UnsupportedDataTypeException, Exception{

        LOG.info(" starting SendEmail sendHtmlMail ");
        LOG.info("entering SendEmail with type = " + type);
    final String mailserver = "relay.proximus.be";
    // https://blogs.oracle.com/apanicker/entry/java_code_for_smtp_server
   
   // String fromEmail = "louis.collet@skynet.be";
    fromEmail = "louis.collet@skynet.be";
  //  final String port = "25"; // normal mail
 //   final String port = "2525"; // mail crypté
    final String port = "587"; // authentification required
    final String username = "louis.collet@skynet.be";
    final String displayName = "GolfLC";
    final String password = "Lm58Spa2"; // new 12/2019 was 9tygru4m";
  //  String password = "lc1lc2lc"; // mod 26/12/2016 was "d"
try{
    Properties props = System.getProperties();
    props.put("mail.smtp.host", mailserver);
    props.put("mail.smtp.from", fromEmail);
    props.put("mail.smtp.port", port);
    
    // set to false on 05-08-2018
    props.put("mail.smtp.debug", "false"); // mettre aussi session.setDebug(true); voir plus loin, debug dans console only
    
    props.put("mail.debug.auth", "true"); // new 24/02/2017 was "true"
    props.put("java.security.debug", "false"); // new 26/12/2016  // was true
    props.put("mail.smtp.timeout", 15000); // millisecs new 09/04/2018 testing purposes ...

    props.put("mail.user", username);
    props.put("mail.password", password);
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

   Session session = Session.getInstance(props, null); // mod 07/03/2017
    // // Create a new message --
    session.setDebug(false); // new 26/12/2016  mod 07-12-2018 was true
        LOG.info("this is a NON debug mail sending !");
    MimeMessage msg = new MimeMessage(session);
    // -- Set the FROM and TO fields --
    msg.setFrom(new InternetAddress(fromEmail, displayName));
    msg.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to, false));

//http://blog.smartbear.com/how-to/how-to-send-email-with-embedded-images-using-java/
//http://stackoverflow.com/questions/3902455/mail-multipart-alternative-vs-multipart-mixed
 //Lastly, we also tell the mail client that the text part and the image part are related
// and should be shown as a single item, not as separate pieces of the message.
// We do so by changing how we create the message content:MimeMultipart content = new MimeMultipart("related");
 //   MimeMultipart content = new MimeMultipart("alternative");
    MimeMultipart multipart = new MimeMultipart("related"); // new 25/12/2016
    MimeBodyPart htmlPart = new MimeBodyPart();
    
    // ContentID is used by both parts
    String cid = ContentIdGenerator.getContentId();
//MIME -- Multipurpose Internet Mail Extensions (MIME) is an Internet Standard for the format of e-mail.
//Virtually all Internet e-mail is transmitted via SMTP in MIME format.
//Internet e-mail is so closely associated with the SMTP and MIME standards
//that it is sometimes called SMTP/MIME e-mail.
    htmlPart.setHeader("MIME-Version","1.0" ); 
 //       LOG.info(" SendEmail sendHtmlMail texte 1 = " + texte);
    texte = texte + "<html><div>And <b>here</b>'s an image: <img src=\"cid:" + cid + "\"></div></html>";
 ///       LOG.info(" SendEmail sendHtmlMail texte = " + texte);
    htmlPart.setContent(texte,"text/html; charset=utf-8");
    multipart.addBodyPart(htmlPart);
    // Image part - new 25/12/2016
    MimeBodyPart imagePart = new MimeBodyPart();
    DataSource fds = new FileDataSource(Constants.images_library + "golf man drive.jpg"); //teapot.jpg");
    imagePart.setDataHandler(new DataHandler(fds));
    
    //msg.setDataHandler(new DataHandler(new FileDataSource(file)));  //also for text from a file !!!
    imagePart.setHeader("Content-ID", "<image>");
    imagePart.setContentID("<" + cid + ">");
  //  Notice how we tell mail clients that the image is to be displayed inline
  // (not as an attachment)
    imagePart.setDisposition(MimeBodyPart.INLINE);
    multipart.addBodyPart(imagePart);
    
  //  File attachment
  if (type.equals("INSCRIPTION")){    // attachment .ics file
         MimeBodyPart attachmentPart = new MimeBodyPart();
         String filename = "c:\\aa (LC Data)\\GolfCalendar.ics";
         DataSource source = new FileDataSource(filename);
         attachmentPart.setDataHandler(new DataHandler(source));
     //    htmlPart.setFileName(filename);
         attachmentPart.setFileName("from GolfLc = " + filename);      // si omis , alors "part 1.2"
         multipart.addBodyPart(attachmentPart);
  }

    msg.setContent(multipart);
    msg.setHeader("MIME-Version" ,"1.0");
    msg.setHeader("X-Mailer", "GolfLC Custom Mailer");
    msg.setHeader("Precedence", "bulk"); // 26/12/2016
    msg.setHeader("Auto-Submitted", "auto-generated"); // 26/12/2016
    msg.setHeader("Content-Transfer-Encoding", "base64");
    msg.setSubject(sujet);
    msg.setSentDate(new Date());
 
    Transport tra = session.getTransport("smtp");
    tra.connect(mailserver, username, password);
    msg.saveChanges();
    // Output now correct.    

//    LOG.info( "HTML = text/html : " + htmlPart.isMimeType( "text/html" ) );
//    LOG.info( "HTML Content Type: " + htmlPart.getContentType() );
 //   LOG.info( "HTML Data Handler: " + htmlPart.getDataHandler().getContentType() );
 //   LOG.info( "Image Data Handler: " + imagePart.getDataHandler().getContentType() );
    
    // -- Send the message --
    tra.sendMessage(msg, msg.getAllRecipients() );
    tra.close();

    return true;
}catch (MessagingException e){
    LOG.error("MessagingException in sendHtmlMail = " + e.getMessage(), e);
    throw e;
}catch (Exception e){
    LOG.error("Exception in sendHtmlMail = " + e.getMessage(), e);
    throw e;
}
} // end method


public static void main(String[] args) throws Exception // for testing purposes
{
//final File f = new File("C:/AAA10 Aberdeen.doc");
//File f = null;
//final SendEmail_old sm = new SendEmail_old("Mail title with attachment","Mail text",f);
//sm.sendOneMail(); //"Mail title","Mail text");
   String text = "from main - Mail text <b> bold ??? </b>"
        + "</br> really new line ?"
        + "</br> now italic : " + " </br> now <i> italiques </i>";
//final SendEmail_old sm2 = new SendEmail_old("Mail title without attachment",text);
//sm2.sendOneMail(); //"Mail title","Mail text");
//LOG.info("msg sent = " + text);
   String sujet = "Ceci est le sujet du mail, louis";
   String to = "louis.collet@skynet.be";
   utils.SendEmail sm = new utils.SendEmail();
} // end main

private void cleanUp()
{
    //LOG.info(" from cleanUp - SendEmail_old = ");
    // Force garbage collection & finalization:
    //System.gc();
    //attachment = null;
}

} // end class SendEmail_old
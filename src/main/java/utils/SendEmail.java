package utils;

import java.io.*;
import java.util.*;
import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.activation.UnsupportedDataTypeException;
import javax.mail.*;
import javax.mail.internet.*;
import lc.golfnew.Constants;

public class SendEmail implements interfaces.Log{
    
  //  final private String subject; //= msgSubject;
    private String text; // = msgText;
 //   private static String password;
    private static String fromEmail;
    private File attachment;
/*
public SendEmail(String subject, String text, File attachment) // constructor #1 with attachment
{
        LOG.info(" SendEmail with Attachment");
    this.subject = subject;
    this.text = text;
    this.attachment = attachment;
    if (attachment == null)
     LOG.info(" error !! Attachment = null");
}

public SendEmail(String subject, String text) // constructor #2 without attachment
{
        LOG.info(" SendEmail Text Only");
    this.subject = subject;
 ///   this.text = text;
    //this.attachment = attachment;
}
*/
public boolean sendHtmlMail(final String sujet, String texte, final String  to) throws UnsupportedDataTypeException, MessagingException, Exception
{
        LOG.info(" starting SendEmail sendHtmlMail ");
    final String mailserver = "relay.proximus.be";
    // https://blogs.oracle.com/apanicker/entry/java_code_for_smtp_server
   
   // String fromEmail = "louis.collet@skynet.be";
    fromEmail = "louis.collet@skynet.be";
  //  final String port = "25"; // normal mail
 //   final String port = "2525"; // mail crypté
     final String port = "587"; // authentification required
    final String username = "louis.collet@skynet.be";
    final String displayName = "GolfLC";
    final String password = "9tygru4m";
  //  String password = "lc1lc2lc"; // mod 26/12/2016 was "d"
try
{
    Properties props = System.getProperties();
    props.put("mail.smtp.host", mailserver);
    props.put("mail.smtp.from", fromEmail);
    props.put("mail.smtp.port", port);
    
    // set to false on 05-08-2018
    props.put("mail.smtp.debug", "false"); // mettre aussi session.setDebug(true); voir plus loin, debug dans console only
    
    props.put("mail.debug.auth", "true"); // new 24/02/2017 was "true"
    props.put("java.security.debug", "true"); // new 26/12/2016
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
    session.setDebug(true); // new 26/12/2016
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
        LOG.info(" SendEmail sendHtmlMail texte = " + texte);
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
}catch (Exception e){
    LOG.error("Error SendEmail = " + e.getMessage(), e);
    throw e;
}
} // end method

// new 26/12/2016
//private static class SMTPAuthenticator extends javax.mail.Authenticator
//{
//public PasswordAuthentication getPasswordAuthentication()
//{
//    return new PasswordAuthentication(fromEmail, password);
//}
//}

/*
public void sendOneMail() throws UnsupportedDataTypeException, MessagingException
{
    final Properties p = new Properties();
    //String host = "relay.skynet.be";
    p.put("mail.smtp.host", "relay.skynet.be");
    p.put("mail.smtp.port", "25");
    p.put("mail.mime.charset", "ISO-8859-1");

try
{
    final Session ses = Session.getDefaultInstance(p, null);
    ses.setDebug(false);
// create a message
    final MimeMessage msg = new MimeMessage(ses);
    String from = "louis.collet@skynet.be";
    msg.setFrom(new InternetAddress(from));
    String to = "louis.collet@skynet.be";
    msg.setRecipients(Message.RecipientType.TO, to);
    msg.setSubject(subject);
// create and fill the first message part
    final Multipart mp = new MimeMultipart();
    BodyPart mbp1 = new MimeBodyPart();
    mbp1.setText(text);  // Contenu du message
    //Ajout de la première partie du message dans un objet Multipart

    // Attach the part to the multipart
    mp.addBodyPart(mbp1);

    if (attachment != null)         // create the second message part
    {
        BodyPart mbp2 = new MimeBodyPart();
        final DataSource ds = new FileDataSource(attachment);
        mbp2.setDataHandler(new DataHandler(ds));
        mbp2.setFileName("from GolfLc = " + attachment.toString());      // si omis , alors "part 1.2"
        //Attach the part to the multipart
        mp.addBodyPart(mbp2);
    }
    // Add the Multipart to the message
	    // create the Multipart and add its parts to it

    //msg.setContent(mp);
    msg.setContent(mp,"text/html"); //mod 10/02/2013

// send the message
    Transport.send(msg);
    LOG.info(" -- mail sent ! ");
} // end try
//catch (UnsupportedDataTypeException dte)
//    {LOG.info(" -- UnsupportedDataTypeException by LC");
//        dte.printStackTrace();
//        throw dte;
//    } // end catch
catch (MessagingException me)
    {LOG.info(" -- MessagingException by LC");
        throw me;
    } // end catch
finally
    {
        LOG.info(" -- finally by LC");
    }    // cleanUp();}
    //catch (IOException e)
    //{
    //    e.printStackTrace();
    //} // end catch
  } // end class SendOneMail
*/

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
   boolean b = sm.sendHtmlMail(sujet,text,to);
//LOG.info("msg sent = " + b);
} // end main

private void cleanUp()
{
    //LOG.info(" from cleanUp - SendEmail_old = ");
    // Force garbage collection & finalization:
    //System.gc();
    //attachment = null;
}

} // end class SendEmail_old
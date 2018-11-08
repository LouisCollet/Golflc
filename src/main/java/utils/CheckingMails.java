/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package utils;

/**
 *
 * @author collet
 */

import java.util.Properties;
import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.NoSuchProviderException;
import javax.mail.Session;
import javax.mail.Store;

public class CheckingMails implements interfaces.Log {

   public void check(String host, String storeType, String user,
      String password) 
   {
      try {

      // get the session object
      Properties properties = new Properties();

      properties.put("mail.pop3.host", host);
      properties.put("mail.pop3.port", "587");  //was 995
      properties.put("mail.pop3.starttls.enable", "true");
      Session emailSession = Session.getDefaultInstance(properties);
  
      //create the POP3 store object and connect with the pop server
      Store store = emailSession.getStore("pop3s"); // s=encrypted

      store.connect(host, user, password);

      //create the folder object and open it
      Folder emailFolder = store.getDefaultFolder().getFolder("INBOX");
      emailFolder.open(Folder.READ_WRITE); // nécessaire for DELETE  // was READ_ONLY

 /*     
      // search for all "unseen" messages
    Flags seen = new Flags(Flags.Flag.SEEN);
    FlagTerm unseenFlagTerm = new FlagTerm(seen, false);
    Message messagesu[] = emailFolder.search(unseenFlagTerm);
     
    if (messagesu.length == 0) System.out.println("No unread messages found.");
 
    for (int i = 0; i < messagesu.length; i++) {
      System.out.println("Message unread " + (i + 1));
      System.out.println("From : " + messagesu[i].getFrom()[0]);
      System.out.println("Subject : " + messagesu[i].getSubject());
      System.out.println("Sent Date : " + messagesu[i].getSentDate());
      System.out.println();
    }

        emailFolder.close(false);
        store.close();
*/
            
      // retrieve the messages from the folder in an array and print it
      Message[] messages = emailFolder.getMessages();
      LOG.debug("# messages = " + messages.length);
      LOG.debug("# messages = " + emailFolder.getMessageCount() );
      LOG.debug("# unread messages = " + emailFolder.getUnreadMessageCount() );  // pas juste !
      for (int i=0, n=messages.length; i<n; i++) {
         Message message = messages[i];
         LOG.debug("---------------------------------");
         LOG.debug("Email Number " + (i + 1));
         String from = "";
      if (message.getReplyTo().length >= 1)
        {
          from = message.getReplyTo()[0].toString();
          LOG.debug("From Reply To: " + from);
      }else if (message.getFrom().length >= 1)
        {
          from = message.getFrom()[0].toString();
          LOG.debug("From nFrom: " + from);
      }
         if(from.contains("<")) // from entre < et >
         {
            from = from.substring(from.indexOf("<")+1, from.indexOf(">"));
            LOG.debug("From2: " + from);
         }else{
            LOG.debug("From2: " + from);
         }
         LOG.debug("From after : " + from);
         
         String subject = message.getSubject();
            LOG.debug("Subject: " + subject );
         subject = subject.trim().toLowerCase();
            LOG.debug("Subject after : " + subject );
            LOG.debug("Content : " + message.getContentType());
            boolean isMessageRead = true;
            for (Flags.Flag flag : message.getFlags().getSystemFlags()) {
                if (flag == Flags.Flag.SEEN) {
                    isMessageRead = true;
                    break;
                }
            }
            LOG.debug("read/unread" + message.getSubject() + " " + (isMessageRead ? " [READ]" : " [UNREAD]"));
         if(subject.equals("unsubscribe"))
         {
            LOG.debug("equals unsubscribe " + subject);
            // envoyer un mail de confirmayion de réception
            utils.SendEmail sm = new utils.SendEmail();
            boolean b = sm.sendHtmlMail("unsubscribe confirmation : " + from, "mailbody = unsubscribe confirmation", "louis.collet@skynet.be","CHECKING");
               LOG.info("HTML Mail status = " + b);
            // delete logique du mail current
            message.setFlag(Flags.Flag.DELETED, true);
               LOG.info("Message marked deleted for : " + from);
         }
       
         
 //        String from = InternetAddress.toString(message.getFrom() );
 //        LOG.debug("From2: " + from);
         LOG.debug("Sent: " + message.getSentDate().toString() );
//         LOG.debug("Text: " + message.getContent().toString() );
      }
      //close the store and folder objects
      // true = expunges the folder to remove messages which are marked deleted
      emailFolder.close(true);
      store.close();
      LCUtil.showDialogInfo("end CheckingMails !!");
 } catch (NoSuchProviderException e) {
              String msg = "NoSuchProviderException " + e;
              LOG.error(msg);
              LCUtil.showMessageFatal(msg);
 } catch (MessagingException e) {
              String msg = "MessagingException " + e;
              LOG.error(msg);
              LCUtil.showMessageFatal(msg);
 } catch (Exception e) {
              String msg = "Exception " + e;
              LOG.error(msg);
              LCUtil.showMessageFatal(msg);
      }
   }

   public static void main(String[] args) {

      String host = "pop.skynet.be";// change accordingly
      String mailStoreType = "pop3";
      String username = "louis.collet@skynet.be";// change accordingly
      String password = "lc1lc2lc";// change accordingly
      CheckingMails cm = new CheckingMails();
      cm.check(host, mailStoreType, username, password);

   }

}

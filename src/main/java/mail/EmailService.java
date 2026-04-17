package mail;

import entite.Club;
import entite.Course;
import entite.Player;
import entite.Round;
import static exceptions.LCException.handleGenericException;
import static interfaces.Log.LOG;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.activation.DataHandler;
import jakarta.activation.DataSource;             // ✅ CORRECT - pour email (pas DB)
import jakarta.annotation.PostConstruct;
import jakarta.mail.*;
import jakarta.mail.internet.*;
import jakarta.mail.util.ByteArrayDataSource;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Properties;
// ✅ SUPPRIMÉ : import connection_package.DBConnection;
// ✅ SUPPRIMÉ : import java.sql.Connection;

@ApplicationScoped
public class EmailService {
   
    @Inject private utils.QRCodeService qrService;
    // ✅ AJOUTÉ : injection CDI ReadClub
    @Inject private read.ReadClub readClubService;
    @Inject private entite.Settings settings;        // ✅ injection CDI
    
    @PostConstruct
    void init() {
        LOG.debug("Postconstruct - qrService = " + qrService);
    }
    
    public boolean sendHtmlMail(
            String title,
            String htmlContent,
            final String recipient,
            byte[] icsAttachment,
            final String qrContent,
            final String targetLanguage
    ) {
        final String methodName = utils.LCUtil.getCurrentMethodName(); 
        try {
            LOG.debug("entering sendHtmlMail");
            
            if (!"fr".equalsIgnoreCase(targetLanguage)) {
                title = translate(title, targetLanguage);
                htmlContent = translate(htmlContent, targetLanguage);
            }

            Properties props = new Properties();
            props.put("mail.smtp.host", "relay.proximus.be");
            props.put("mail.smtp.port", "587");
            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.starttls.enable","true");

            final String username = settings.getProperty("SMTP_USERNAME");
            final String password = settings.getProperty("SMTP_PASSWORD");

            Session session = Session.getInstance(props, new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(username, password);
                }
            });
            LOG.debug("after session");
            
            MimeMessage msg = new MimeMessage(session);
            msg.setFrom(new InternetAddress(username, "GolfLC"));
            msg.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipient, false));
            msg.setSubject(title, "UTF-8");
            msg.setSentDate(new Date());

            MimeMultipart multipart = new MimeMultipart("mixed");

            // 1️⃣ Corps HTML
            MimeBodyPart htmlPart = new MimeBodyPart();
            htmlPart.setContent(htmlContent, "text/html; charset=utf-8");
            multipart.addBodyPart(htmlPart);

            // 2️⃣ QR Code inline + attachment
            if (qrContent != null && !qrContent.isBlank()) {
                byte[] qrBytes = qrService.generateQR(qrContent, 200);

                MimeBodyPart qrPart = new MimeBodyPart();
                DataSource qrDs = new ByteArrayDataSource(qrBytes, "image/png");
                qrPart.setDataHandler(new DataHandler(qrDs));
                qrPart.setFileName("QRCode.png");
                qrPart.setDisposition(MimeBodyPart.ATTACHMENT);
                multipart.addBodyPart(qrPart);
            }

            // 3️⃣ ICS attachment
            if (icsAttachment != null) {
                MimeBodyPart icsPart = new MimeBodyPart();
                DataSource icsDs = new ByteArrayDataSource(icsAttachment, "text/calendar; charset=utf-8");
                icsPart.setDataHandler(new DataHandler(icsDs));
                icsPart.setFileName("appointment.ics");
                multipart.addBodyPart(icsPart);
            }

            msg.setContent(multipart);
            msg.saveChanges();

            Transport.send(msg);
            return true;

        } catch (Exception e) {
            //  e.printStackTrace();
            handleGenericException(e, methodName);
            return false;
        }
    }

    private String translate(String text, String lang) throws IOException, GeneralSecurityException {
        return translation.FileTranslation.translateList(Arrays.asList(text), lang);
    }
    
    void main(String[] args) throws Exception {
        // LOG.debug("args = " + Arrays.toString(args));
        // ✅ SUPPRIMÉ : Connection conn = new DBConnection().getConnection();
        try {
            long start = System.nanoTime();
            String content = "Ceci est le texte du mail <b> gras </b>"
                + "</br> really new line ?"
                + "</br> now italic : " 
                + " </br> now <i> italiques </i>";
            String title = "Ceci est le sujet du mail, louis";
            String recipient = settings.getProperty("SMTP_USERNAME") + "," + settings.getProperty("SMTP_USERNAME_ONDUTY");
            
            String qrContent = "</br>this is the start of the content" + content 
                              + "</br>this is the end of the content";
            
            Player player = new Player();
            player.setIdplayer(456783);
            player.setPlayerLastName("Muntingh");
            player.setPlayerLanguage("fr");
            player.setPlayerEmail("theo.muntingh@skynet.be");
            Player player2 = new Player();
            player2.setIdplayer(2014101);
            Player player3 = new Player();
            player3.setIdplayer(2014102);  
            ArrayList<Player> p = new ArrayList<>();
            p.add(player2);
            p.add(player3);
            player.setDroppedPlayers(p);
         
            Player invitedBy = new Player();
            invitedBy.setIdplayer(324713);
            player.setPlayerLastName("Collet");

            Club club = new Club();
            club.setIdclub(108);  // rigenée
            // ✅ CORRIGÉ : injection CDI (plus de new + conn)
            club = readClubService.read(club);
            
            Course course = new Course();
            Round round = new Round(); 
            round.setRoundDate(LocalDateTime.of(2018, Month.NOVEMBER, 17, 12, 15));
            round.setRoundGame("round game : STABLEFORD");
            round.setPlayersString("inscrits précédemment : Corstjens, Bauer");
         
            byte[] icsAttachment = new ical.IcalService().generateIcs(player, invitedBy, round, club, course, true);
            
            boolean b = new mail.EmailService().sendHtmlMail(title, content, recipient, icsAttachment, qrContent, "es");
            
            LOG.debug("boolean result = " + b);
            long elapsedNanos = System.nanoTime() - start;
            LOG.debug("Elapsed time Nanos: " + elapsedNanos);
            double elapsedMillis = elapsedNanos / 1_000_000.0;
            LOG.debug("Elapsed time Millis: " + elapsedMillis + " ms");
            
        } catch (Exception e) {
            String msg = "££ Exception in main = " + e.getMessage();
            LOG.error(msg);
        }
        // ✅ SUPPRIMÉ : finally { DBConnection.closeQuietly(conn, null, null, null); }
    } // end main
} // end class
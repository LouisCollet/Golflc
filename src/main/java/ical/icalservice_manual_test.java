package ical;

import connection_package.DBConnection2;
import entite.Club;
import entite.Course;
import entite.Player;
import entite.Round;
import static interfaces.Log.LOG;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import static utils.LCUtil.showMessageFatal;
import static utils.LCUtil.showMessageInfo;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.ArrayList;
import java.util.List;
import manager.PlayerManager;

/**
 * Classe de test manuel pour IcalService.
 * <p>
 * Cette classe permet de tester la génération d'invitations iCalendar
 * avec des données réelles depuis la base de données.
 * </p>
 * <p>
 * <strong>Note:</strong> Pour les tests automatisés, utilisez plutôt
 * une classe de test JUnit avec Arquillian.
 * </p>
 */
@RequestScoped // à vlider
public class icalservice_manual_test {
     @Inject
    private PlayerManager playerManager;
    /**
     * Test manuel de génération et envoi d'une invitation iCalendar.
     * <p>
     * Ce test :
     * <ol>
     *   <li>Crée des objets de test (Player, Club, Course, Round)</li>
     *   <li>Charge des données réelles depuis la base de données</li>
     *   <li>Génère un fichier .ics</li>
     *   <li>Envoie l'invitation par email</li>
     * </ol>
     * </p>
     *
     * @param args arguments de ligne de commande (non utilisés)
     */
    public static void main(String[] args) throws SQLException {
        Connection conn = null;
        
        try {
            LOG.info("=== Starting IcalService Manual Test ===");
            
            // Connexion à la base de données
            conn = DBConnection2.getConnection();
            
            // Configuration du test
            TestData testData = createTestData(conn);
            
            // Génération du fichier ICS
            byte[] icsFile = generateIcsFile(testData);
            
            if (icsFile == null || icsFile.length == 0) {
                throw new IllegalStateException("Generated ICS file is empty");
            }
            
            LOG.info("ICS file generated successfully ({} bytes)", icsFile.length);
            
            // Envoi de l'email (optionnel - décommenter si Settings est configuré)
            // sendTestEmail(testData, icsFile);
            
            LOG.info("=== Test completed successfully ===");
            
        } catch (Exception e) {
            String msg = "Error during IcalService manual test: " + e.getMessage();
            LOG.error(msg, e);
            showMessageFatal(msg);
            
        } finally {
            DBConnection2.closeQuietly(conn, null, null, null);
        }
    }
    
    /**
     * Crée les données de test nécessaires.
     */
    private static TestData createTestData(Connection conn) throws Exception {
        LOG.debug("Creating test data...");
        
        TestData data = new TestData();
        
        // Configuration du joueur principal
        data.player = new Player();
        data.player.setIdplayer(456783);
        data.player.setPlayerLastName("Muntingh");
        data.player.setPlayerFirstName("Theo");
        data.player.setPlayerLanguage("fr");
        data.player.setPlayerEmail("theo.muntingh@skynet.be");
        
        // Configuration des joueurs supplémentaires
        Player player2 = new Player();
        player2.setIdplayer(2014101);
        
        Player player3 = new Player();
        player3.setIdplayer(2014102);
        
        ArrayList<Player> droppedPlayers = new ArrayList<>();
        droppedPlayers.add(player2);
        droppedPlayers.add(player3);
        data.player.setDroppedPlayers(droppedPlayers);
        
        // Configuration de l'inviteur
        data.invitedBy = new Player();
        data.invitedBy.setIdplayer(324713);
        data.invitedBy.setPlayerLastName("Collet");
        
        // Chargement du club depuis la DB
        data.club = new Club();
        data.club.setIdclub(108); // Rigenée
        data.club = new read.ReadClub().read(data.club);
        LOG.debug("Loaded club: {}", data.club.getClubName());
        
        // Configuration du parcours
        data.course = new Course();
        // Charger le parcours si nécessaire
        
        // Configuration du round
        data.round = new Round();
        data.round.setRoundDate(LocalDateTime.of(2025, Month.JUNE, 15, 14, 30));
        data.round.setRoundGame("STABLEFORD");
        data.round.setPlayersString("Corstjens, Bauer, Muntingh");
        
        // Chargement des joueurs du flight depuis la DB
        Player p1 = new Player();
        p1.setIdplayer(456784);
     //   p1 = new read.ReadPlayer().read(p1, conn);
/// enlevé       p1 = playerManager.readPlayer(p1.getIdplayer());

        Player p2 = new Player();
        p2.setIdplayer(456785);
      //  p2 = new read.ReadPlayer().read(p2, conn);
///  enlevé      p2 = playerManager.readPlayer(p2.getIdplayer());
        data.round.setPlayers(List.of(p1, p2));
        
        LOG.debug("Test data created successfully");
        
        return data;
    }
    
    /**
     * Génère le fichier ICS.
     */
    private static byte[] generateIcsFile(TestData data) throws Exception {
        LOG.debug("Generating ICS file...");
        
        ical.IcalService icalService = new IcalService();
        
        // true = réservation, false = annulation
        byte[] icsFile = icalService.generateIcs(
            data.player,
            data.invitedBy,
            data.round,
            data.club,
            data.course,
            true
        );
        
        return icsFile;
    }
    
    /**
     * Envoie l'email de test avec l'invitation.
     * <p>
     * Cette méthode nécessite que Settings soit correctement configuré.
     * </p>
     */
    @SuppressWarnings("unused")
    private static void sendTestEmail(TestData data, byte[] icsFile) throws Exception {
        LOG.debug("Sending test email...");
        
        String recipient = "louis.collet@skynet.be, louis.collet.onduty@gmail.com";
        String subject = "Test Golf Invitation - IcalService";
        String body = "Ceci est un email de test pour l'invitation iCalendar";
        byte[] qrCode = null; // Pas de QR code pour ce test
        
        // Nécessite que Settings soit initialisé
        // Settings.init();
        
        boolean sent = new mail.MailSender().sendHtmlMail(
            subject,
            body,
            recipient,
            icsFile,
            qrCode,
            data.player.getPlayerLanguage()
        );
        
        if (sent) {
            String msg = "Email de confirmation envoyé avec succès";
            LOG.info(msg);
            showMessageInfo(msg);
        } else {
            String msg = "Échec de l'envoi de l'email de confirmation";
            LOG.error(msg);
            showMessageFatal(msg);
        }
    }
    
    /**
     * Classe interne pour encapsuler les données de test.
     */
    private static class TestData {
        Player player;
        Player invitedBy;
        Round round;
        Club club;
        Course course;
    }
}

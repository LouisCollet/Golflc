
package Controller.refact;

import context.ApplicationContext;
import entite.Club;
import entite.Course;
import entite.Player;
import entite.Round;
import jakarta.enterprise.context.SessionScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.TimeUnit;
import manager.PlayerManager;
import org.primefaces.model.FilterMeta;
import static exceptions.LCException.handleGenericException;
import static interfaces.Log.LOG;
import static utils.LCUtil.showMessageFatal;
import static utils.LCUtil.showMessageInfo;

/**
 * Controller JSF pour les fonctionnalités techniques : filtres, debug, admin tools.
 * Migré progressivement depuis CourseController.
 */
@Named("techC")
@SessionScoped
public class TechnicalController implements Serializable {

    private static final long serialVersionUID = 1L;

    private List<FilterMeta> filterMeta = new ArrayList<>();

    // ✅ Injections SendEmailTest/numberText — migrated 2026-02-27
    @Inject private NavigationController        navigationController; // migrated 2026-02-28
    @Inject private ApplicationContext          appContext;
    @Inject private Controllers.LanguageController languageController; // fix multi-user 2026-03-07
    @Inject private PlayerManager               playerManager;
    @Inject private read.ReadClub               readClubService;
    @Inject private ical.IcalService            icalService;
    @Inject private mail.MailSender             mailSender;
    @Inject private numbertext.Numbertext       numbertextService;
    @Inject private lists.AuditConnectionList  auditConnectionList;
    @Inject private entite.Settings            settings;

    public TechnicalController() { } // constructeur public obligatoire

    // ========================================
    // FILTER META
    // ========================================

    public List<FilterMeta> getFilterMeta() {
        return filterMeta;
    } // end method

    public void setFilterMeta(List<FilterMeta> filterMeta) {
        this.filterMeta = filterMeta;
    } // end method

    // ========================================
    // DEBUG / TEST (2 méthodes)
    // migrated from CourseController 2026-02-25
    // ========================================

    public void checkMail(String ini) {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        try {
            LOG.debug("starting checkMail with: {}", ini);
            // utils.CheckingMails.main(ini); // argument bidon !!
            LOG.debug("ending checkMail with: {}", ini);
        } catch (Exception e) {
            handleGenericException(e, methodName);
        }
    } // end method

    public void newMessageFatal(String ini) {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        try {
            LOG.debug("starting newMessageFatal with: {}", ini);
            LOG.debug("{}", ini);
            LOG.debug("ending newMessageFatal with: {}", ini);
        } catch (Exception e) {
            handleGenericException(e, methodName);
        }
    } // end method

    // ========================================
    // SendEmailTest / numberText — migrated 2026-02-27 from CourseController
    // ========================================

    public void sendEmailTest() {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        try {
            long start = System.nanoTime();
            String content = "Ceci est le texte du mail <b> gras </b>"
                    + "</br> really new line ?"
                    + "</br> now italic : "
                    + " </br> now <i> italiques </i>";
            String title = "Ceci est le sujet du mail, louis";
            String recipient = settings.getProperty("SMTP_USERNAME") + "," + settings.getProperty("SMTP_USERNAME_ONDUTY");

            String qrContent = "</br>this is the start of the content" + content + "</br>this is the end of the content";

            Player testPlayer = new Player();
            testPlayer.setIdplayer(456783);
            playerManager.readPlayer(testPlayer.getIdplayer());

            Player player2 = new Player();
            player2.setIdplayer(2014101);
            Player player3 = new Player();
            player3.setIdplayer(2014102);
            ArrayList<Player> p = new ArrayList<>();
            p.add(player2);
            p.add(player3);
            testPlayer.setDroppedPlayers(p);
            testPlayer.setPlayerLastName("Collet");

            Player invitedBy = new Player();

            Club club = new Club();
            club.setIdclub(101);
            club = readClubService.read(club);
            LOG.debug("club = {}", club);
            Course course = new Course();
            Round round = new Round();
            round.setRoundDate(LocalDateTime.of(2025, Month.NOVEMBER, 17, 12, 15));
            round.setRoundGame("round game : STABLEFORD");
            round.setPlayersString("inscrits précédemment : Corstjens, Bauer");

            byte[] icsAttachment = icalService.generateIcs(testPlayer, invitedBy, round, club, course, true);
            showMessageInfo("after icalService");
            LOG.debug("after generation of icsAttachment via icalService = {}", icsAttachment);

            showMessageInfo("show MessageInfo - Envoi du mail, Le mail est en cours d'envoi à " + recipient);

            CompletableFuture<Void> cf =
                    mailSender.sendHtmlMailAsync(
                            title,
                            content,
                            recipient,
                            icsAttachment,
                            null,
                            "es"
                    );

            cf.orTimeout(300, TimeUnit.SECONDS)
                    .whenComplete((r, ex) -> {
                        if (ex == null) {
                            LOG.info("Mail envoyé avec succès à {}", recipient);
                        } else {
                            Throwable cause = ex instanceof CompletionException && ex.getCause() != null
                                    ? ex.getCause()
                                    : ex;
                            LOG.error("Mail KO pour {} (timeout ou erreur)", recipient, cause);
                        }
                    });

            LOG.debug("Mail submission for {} done (async)", recipient);
            LOG.debug("CompletableFuture result after Async= {}", cf);

            showMessageInfo("after MailSender");
            long elapsedNanos = System.nanoTime() - start;
            LOG.debug("Elapsed time Nanos: {}", elapsedNanos);
            double elapsedMillis = elapsedNanos / 1_000_000.0;
            LOG.debug("Elapsed time Millis: {} ms", elapsedMillis);
        } catch (Exception ex) {
            String msg = "SendEmailTest" + ex;
            LOG.error(msg);
            showMessageFatal(msg);
        }
    } // end method

    public void numberText(String s) {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        try {
            LOG.debug("locale language = {}", languageController.getLanguage()); // fix multi-user 2026-03-07

            String[] args = new String[3];
            args[0] = "-l";
            args[1] = languageController.getLanguage(); // fix multi-user 2026-03-07
            args[2] = s;

            String result = numbertextService.kernel(args);
            LOG.debug("result = {}", result);

        } catch (Exception e) {
            handleGenericException(e, methodName);
        }
    } // end method

    // ========================================
    // NAVIGATION to_* — migrated 2026-02-28
    // ========================================

    /**
     * Navigation générique vers une page statique/test.
     * Reset la session avant navigation (PRG pattern).
     * @param page le nom du fichier xhtml (sans extension)
     */
    // ========================================
    // AUDIT — connection history
    // ========================================

    public java.util.List<entite.Audit> listAuditConnections() {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        try {
            // No cache — AuditConnectionList.list() already returns fresh DB data
            // Caching here caused stale "connected" status for players who logged in after page load
            java.util.List<entite.Audit> list = auditConnectionList.list();
            LOG.debug("loaded {} audit entries", list.size());
            return list;
        } catch (Exception e) {
            handleGenericException(e, methodName);
            return java.util.Collections.emptyList();
        }
    } // end method

    public String to_page_xhtml(String page) {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {} with page = {}", page);
        navigationController.reset("Reset to_page " + page);
        return page + ".xhtml?faces-redirect=true";
    } // end method

    /**
     * Navigation vers editingRow avec paramètres cmd et operation.
    enlevé 30-03-2026
    public String to_editingRow_xhtml(String cmd, String operation) {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering cmd={} operation={}", cmd, operation);
        navigationController.reset("Reset to_editingRow " + cmd);
        return "editingRow.xhtml?faces-redirect=true&cmd=" + cmd + "&operation=" + operation;
    } // end method
 */
} // end class

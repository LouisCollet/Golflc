package Controller.refact;

import context.ApplicationContext;
import entite.Subscription;
import entite.composite.EPlayerPassword;
import enumeration.SelectionPurpose;
import static exceptions.LCException.handleGenericException;
import static interfaces.Log.LOG;
import jakarta.enterprise.context.SessionScoped;
import jakarta.enterprise.event.Observes;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.servlet.http.HttpServletRequest;
import java.io.Serializable;
import java.util.Locale;
import java.util.Optional;
import static utils.LCUtil.showMessageFatal;

/**
 * Controller JSF pour le flux login.
 * Migré depuis PlayerController 2026-04-03.
 * Gère : sélection joueur, double-login, session fixation, audit, dialog retour.
 */
@Named("loginC")
@SessionScoped
public class LoginController implements Serializable {
  private static final long serialVersionUID = 1L;

    @Inject private ApplicationContext                            appContext;
    @Inject private Controllers.LanguageController                languageController;
    @Inject private Controllers.PasswordController                passwordController;
    @Inject private payment.PaymentSubscriptionController         paymentSubscriptionController;
    @Inject private lists.AuditConnectionList                     auditConnectionList;
    @Inject private create.CreateAudit                            createAudit;
    @Inject private Controllers.DialogController                  dialogController;
    @Inject private contexte.SelectionContextBean                 clubSelectionContext;
    @Inject private manager.PlayerManager                         playerManager;
    @Inject private Controllers.ActiveLocale                      activeLocale;
    @Inject private find.FindLastAudit                            findLastAudit;
    @Inject private update.UpdateAudit                            updateAudit;

    private boolean showForceLogoutButton   = false;
    private boolean showForceReconnectButton = false;
    private boolean forceLoginAllowed        = false;
    private entite.composite.EPlayerPassword pendingEpp = null;

    public LoginController() { } // constructeur public obligatoire

    public void onReset(@Observes events.ResetEvent event) {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {} — source: {}", event.getSource());
        LOG.debug("LoginController reset done");
    } // end method

    // ========================================
    // PREPARE LOGIN — called from login.xhtml <f:viewAction>
    // Migrated from connection_package.LoginBean 2026-04-04
    // ========================================

    /**
     * Initialise la session avant la page de login.
     * Reset appContext player/playerPro/localAdmin/playerTemp, et locale à EN.
     */
    public void prepareLogin() {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        if (appContext.getPlayer() == null) {
            appContext.setPlayer(new entite.Player());
        }
        appContext.setPlayerPro(new entite.Player());
        appContext.setLocalAdmin(new entite.Player());
        appContext.setPlayerTemp(new entite.Player());
        activeLocale.setLanguageTag("en");
        LOG.debug("session initialized, locale set to EN");
    } // end method

    // ========================================
    // LOGIN FLOW
    // ========================================

    /**
     * Login flow — sélection d'un joueur dans la dataTable (selectPlayer.xhtml).
     * Vérifie password, blocking, subscription, double-login, puis redirige vers welcome.xhtml.
     * @return 
     */
    public String selectPlayer(EPlayerPassword epp) {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        try {
            LOG.debug("player = {}", epp.getPlayer());
            appContext.setPlayer(epp.getPlayer());

            if (appContext.getPlayer().getIdplayer() == null) {
                String err = "player is null = " + appContext.getPlayer();
                LOG.debug("{}", err);
                showMessageFatal(err);
                return null;
            } else {
                LOG.debug("current Player = {}", appContext.getPlayer());
            }
            languageController.setLocale(Locale.of(appContext.getPlayer().getPlayerLanguage()));
            LOG.debug("Language set = {}", appContext.getPlayer().getPlayerLanguage());
            LOG.debug("Language is now = {}", languageController.getLanguage());

            LOG.debug("1. verifying if there is an existing password");
            entite.Password p = passwordController.isExists(epp);
            if (p == null) {
                LOG.debug("password is null ==> going to password_create");
                return "password_create.xhtml?faces-redirect=true";
            }

            LOG.debug("2. verifying if there is a blocking password too many trials");
            if (passwordController.isBlocking(appContext.getPlayer())) {
                return "selectPlayer.xhtml?faces-redirect=true";
            }

            LOG.debug("3. verifying if there is a valid subscription");
            Subscription subscription = paymentSubscriptionController.isExists(appContext.getPlayer());
            LOG.debug("subscription valid ? = {}", subscription);
            if (subscription.isErrorStatus()) {
                LOG.debug("subscription is null ==> going to subscription.xhtml");
                return "subscription.xhtml?faces-redirect=true";
            }
            appContext.setSubscription(subscription);

            // Session fixation protection — rotate JSESSIONID before granting access
            HttpServletRequest request = (HttpServletRequest) FacesContext.getCurrentInstance()
                    .getExternalContext().getRequest();
            request.changeSessionId();
            LOG.debug("session rotated (anti session-fixation), new id = {}", request.getSession(false).getId());

            LOG.debug("3b. check double login");
            Integer sessionAuditId  = (Integer) request.getSession().getAttribute("auditId");
            Integer sessionPlayerId = (Integer) request.getSession().getAttribute("user");
            Integer newPlayerId     = appContext.getPlayer().getIdplayer();

            // Même joueur qui revient (back-button, refresh) dans le même browser → ne pas bloquer
            boolean samePlayerReconnecting = newPlayerId.equals(sessionPlayerId)
                    && sessionAuditId != null
                    && auditConnectionList.isAuditOpen(sessionAuditId);

            // Joueur DIFFÉRENT qui essaie de se connecter dans le même browser (même session) → bloquer
            boolean anotherPlayerInSameBrowser = sessionPlayerId != null && !newPlayerId.equals(sessionPlayerId);

            LOG.debug("sessionAuditId={} sessionPlayerId={} newPlayerId={} samePlayerReconnecting={} anotherPlayerInSameBrowser={}",
                    methodName, sessionAuditId, sessionPlayerId, newPlayerId, samePlayerReconnecting, anotherPlayerInSameBrowser);

            if (anotherPlayerInSameBrowser) {
                LOG.warn("player {} already in session, blocking player {}", sessionPlayerId, newPlayerId);
                String connectedName;
                try {
                    entite.Player connectedPlayer = playerManager.readPlayer(sessionPlayerId);
                    connectedName = " — " + connectedPlayer.getPlayerLastName() + " " + connectedPlayer.getPlayerFirstName();
                } catch (Exception ex) {
                    connectedName = " (id=" + sessionPlayerId + ")";
                }
                showMessageFatal(utils.LCUtil.prepareMessageBean("player.already.connected.browser", connectedName));
                showForceLogoutButton = true;
                return null;
            }

            if (!forceLoginAllowed && !samePlayerReconnecting && auditConnectionList.isPlayerOnline(newPlayerId)) {
                LOG.warn("player {} already connected from another session — login blocked", newPlayerId);
                showMessageFatal(utils.LCUtil.prepareMessageBean("player.already.connected"));
                showForceReconnectButton = true;
                pendingEpp = epp;
                return null;
            }

            LOG.debug("4. initialisations diverses");
            appContext.setConnected(true);
            appContext.getClub().setIdclub(epp.player().getPlayerHomeClub());
            appContext.getPlayer().setShowMenu(true);
            LOG.debug("showmenu = {}", appContext.getPlayer().isShowMenu());

            // Store player ID in HTTP session for SecurityFilter and audit cleanup on session destroy
            request.getSession().setAttribute("user", appContext.getPlayer().getIdplayer());
            LOG.debug("session attribute 'user' set to {}", appContext.getPlayer().getIdplayer());

            LOG.debug("5. create audit log");
            int auditId = createAudit.create(appContext.getPlayer());
            if (auditId > 0) {
                request.getSession().setAttribute("auditId", auditId);
                LOG.debug("audit created id={}, stored in session", auditId);
            }

            return "welcome.xhtml?faces-redirect=true";
        } catch (Exception e) {
            handleGenericException(e, methodName);
            return null;
        }
    } // end method

    /**
     * Login raccourci via le player selector (include_player_selector.xhtml).
     * Cherche l'EPlayerPassword correspondant à playerTemp.idplayer dans la liste,
     * puis délègue à selectPlayer(epp) pour le flux login standard.
     */
    public String selectPlayerById() {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        try {
            Integer id = appContext.getPlayerTemp().getIdplayer();
            LOG.debug("looking up player id = {}", id);
            if (id == null || id == 0) {
                showMessageFatal("Please enter a valid player ID");
                return null;
            }
            EPlayerPassword epp = playerManager.listPlayers().stream()
                    .filter(e -> id.equals(e.getPlayer().getIdplayer()))
                    .findFirst()
                    .orElse(null);
            if (epp == null) {
                showMessageFatal("Player ID " + id + " not found");
                return null;
            }
            return selectPlayer(epp);
        } catch (Exception e) {
            handleGenericException(e, methodName);
            return null;
        }
    } // end method

    // ========================================
    // DIALOG
    // ========================================

    /**
     * Sélection d'un joueur via dialog (dialogPlayer.xhtml).
     * Gère LOCAL_ADMIN, CREATE_PRO, CREATE_PLAYER selon SelectionPurpose.
     */
    public String selectedPlayerFromDialog(EPlayerPassword epp) {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {} with player = {}", epp.getPlayer());
        LOG.debug("with playerTemp = {}", appContext.getPlayerTemp());
        try {
            SelectionPurpose purpose = Optional.ofNullable(clubSelectionContext.getPurpose())
                    .orElse(SelectionPurpose.CREATE_PLAYER);
            LOG.debug("with purpose = {}", purpose);

            if (purpose == SelectionPurpose.LOCAL_ADMIN) {
                LOG.debug("we handle LA");
                appContext.setPlayerTemp(epp.getPlayer());
                LOG.debug("selected local administrator = {}", appContext.getPlayerTemp());
                LOG.debug("select pour localadministrateur - closeDialog version CDI");
                dialogController.closeDialog(null);
                return null;
            }
            if (purpose == SelectionPurpose.CREATE_PRO) {
                appContext.setPlayerTemp(epp.getPlayer());
                dialogController.closeDialog(null);
                return null;
            }
            // login (selectPlayer.xhtml) — close dialog, pass epp back to parent page
            if (purpose == SelectionPurpose.CREATE_PLAYER) {
                appContext.setPlayerTemp(epp.getPlayer());
                dialogController.closeDialog(epp);
                return null;
            }
            LOG.warn("unhandled purpose: {}", purpose);
            return null;
        } catch (Exception e) {
            LOG.error("exception: {}", e.getMessage(), e);
            showMessageFatal("Exception in " + methodName + ": " + e.getMessage());
            return null;
        }
    } // end method

    /**
     * Listener pour dialogReturn du player selector.
     * Si le dialog renvoie un EPlayerPassword (mode login), lance le flux login complet
     * et redirige la page parent vers welcome.xhtml (ou password_create, subscription...).
     */
    public void onPlayerDialogReturn(org.primefaces.event.SelectEvent<Object> event) throws java.io.IOException {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        Object obj = event.getObject();
        if (obj instanceof EPlayerPassword epp) {
            LOG.debug("received EPlayerPassword, launching login flow for player = {}", epp.getPlayer());
            String outcome = selectPlayer(epp);
            if (outcome != null) {
                String url = outcome.replace("?faces-redirect=true", "");
                jakarta.faces.context.ExternalContext ec2 = FacesContext.getCurrentInstance().getExternalContext();
                ec2.redirect(ec2.getRequestContextPath() + "/" + url);
            }
        } else {
            LOG.debug("dialogReturn object is not EPlayerPassword: {}", obj);
        }
    } // end method

    /**
     * Force logout — ferme l'audit du joueur actuellement en session et invalide la session.
     * Appelé quand un joueur différent tente de se connecter sur une session déjà occupée.
     */
    public String forceLogout() {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        try {
            HttpServletRequest request = (HttpServletRequest) FacesContext.getCurrentInstance()
                    .getExternalContext().getRequest();
            Integer sessionPlayerId = (Integer) request.getSession().getAttribute("user");
            if (sessionPlayerId != null) {
                LOG.info("force logout for sessionPlayerId={}", sessionPlayerId);
                entite.Audit a = new entite.Audit();
                a.setAuditPlayerId(sessionPlayerId);
                a = findLastAudit.find(a);
                if (a != null) {
                    updateAudit.stop(a);
                    LOG.debug("audit closed for sessionPlayerId={}", sessionPlayerId);
                }
            }
            showForceLogoutButton = false;
            jakarta.faces.context.FacesContext.getCurrentInstance()
                    .getExternalContext().invalidateSession();
            LOG.info("session force-invalidated");
            return "login.xhtml?faces-redirect=true";
        } catch (Exception e) {
            handleGenericException(e, methodName);
            return null;
        }
    } // end method

    /**
     * Force reconnect — ferme l'audit existant du joueur (autre session/PC) et relance le login.
     */
    public String forceReconnect() {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering");
        try {
            if (pendingEpp == null) {
                showMessageFatal("No pending login — please select your player again");
                return null;
            }
            Integer playerId = pendingEpp.getPlayer().getIdplayer();
            entite.Audit a = new entite.Audit();
            a.setAuditPlayerId(playerId);
            a = findLastAudit.find(a);
            if (a != null) {
                updateAudit.stop(a);
                LOG.info("force reconnect: closed existing audit for playerId={}", playerId);
            }
            showForceReconnectButton = false;
            forceLoginAllowed = true;
            String outcome = selectPlayer(pendingEpp);
            pendingEpp = null;
            forceLoginAllowed = false;
            return outcome;
        } catch (Exception e) {
            handleGenericException(e, methodName);
            return null;
        }
    } // end method

    public boolean isShowForceLogoutButton()    { return showForceLogoutButton; }
    public boolean isShowForceReconnectButton() { return showForceReconnectButton; }

    /*
    void main() {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        // tests locaux
    } // end main
    */

} // end class

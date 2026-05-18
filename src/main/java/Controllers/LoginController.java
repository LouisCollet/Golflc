package Controllers;

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
    @Inject private Controllers.CartController              cartC;
    @Inject private read.ReadClub                           readClubService;

    private boolean showForceLogoutButton   = false;
    private boolean showForceReconnectButton = false;
    private boolean forceLoginAllowed        = false;
    private entite.composite.EPlayerPassword pendingEpp = null;

    public LoginController() { } // constructeur public obligatoire

    public void onReset(@Observes events.ResetEvent event) {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {} — source: {}", methodName, event.getSource());
        LOG.debug("LoginController reset done");
    } // end method

    // ========================================
    // PREPARE LOGIN — called from login.xhtml <f:viewAction>
    // Migrated from connection_package.LoginBean 2026-04-04
    // ========================================

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
                    sessionAuditId, sessionPlayerId, newPlayerId, samePlayerReconnecting, anotherPlayerInSameBrowser);

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
            entite.Club homeClub = new entite.Club();
            homeClub.setIdclub(epp.player().getPlayerHomeClub());
            appContext.setClub(readClubService.read(homeClub));
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

    public String selectedPlayerFromDialog(EPlayerPassword epp) {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
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
            handleGenericException(e, methodName);
            return null;
        }
    } // end method

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

    public String forceReconnect() {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
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

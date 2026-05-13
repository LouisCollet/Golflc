package Controller.refact;

import entite.Audit;
import static exceptions.LCException.handleGenericException;
import static interfaces.GolfInterface.ZDF_TIME;
import static interfaces.Log.LOG;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.servlet.http.HttpServletRequest;

import java.io.Serializable;
import java.nio.charset.Charset;
import context.ApplicationContext;
import jakarta.enterprise.context.SessionScoped;
import utils.LCUtil;

import static utils.LCUtil.showMessageFatal;
import static utils.LCUtil.showMessageInfo;

/**
 * NavigationController — session orchestrator.
 * Manages session lifecycle: init, reset (appContext + cacheInvalidator + ResetEvent), exit.
 * All state fields have been migrated to domain controllers (ClubC, RoundC, PlayerC, MemberC).
 * @since 2026-02-28 — refactored from courseC (massive controller → thin orchestrator)
 */
@Named("navC")
@SessionScoped
public class NavigationController implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject private ApplicationContext appContext;
    @Inject private cache.CacheInvalidator cacheInvalidator;
    @Inject private jakarta.enterprise.event.Event<events.ResetEvent> resetEvent;
    @Inject private find.FindLastAudit findLastAudit;
    @Inject private update.UpdateAudit updateAudit;
    @Inject private contexte.SelectionContextBean clubSelectionContext;
    @Inject private Controllers.LoggingUserController loggingUserController;

    public NavigationController() { }

    @PostConstruct
    public void init() {
        final String methodName = LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        try {
            reset("from init in NavigationController");
            int timeout = FacesContext.getCurrentInstance().getExternalContext().getSessionMaxInactiveInterval();
            LOG.debug("NEW session started — charset={}, session timeout={}s", Charset.defaultCharset(), timeout);
        } catch (Exception e) {
            handleGenericException(e, methodName);
        }
    } // end method

    @PreDestroy
    public void exit() {
        final String methodName = LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
    } // end method

    /**
     * Central reset — resets appContext, invalidates all caches, fires ResetEvent.
     * Each sub-controller observes ResetEvent and resets its own fields.
     */
    public void reset(String ini) {
        final String methodName = LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        try {
            LOG.debug("starting reset with: {}", ini);
            appContext.reset();
            cacheInvalidator.invalidateAll();
            loggingUserController.resetText("first start");

            if (appContext.getPlayer() != null) {
                appContext.getPlayer().clearDroppedPlayers();
                appContext.getPlayer().clearDraggedPlayers();
            }

            resetEvent.fire(new events.ResetEvent(ini));
            LOG.debug("ResetEvent fired for: {}", ini);
        } catch (Exception e) {
            handleGenericException(e, methodName);
        }
    } // end method

    // ========================================
    // NAVIGATION — SelectionPurpose routing
    // Moved from ClubController 2026-03-23
    // ========================================

    /**
     * Central navigation method — resolves SelectionPurpose from menu code,
     * opens CDI context, and delegates navigation to the enum.
     * Called from all menu items via #{navC.to_selectPurpose_xhtml('CODE')}.
     */
    public String to_selectPurpose_xhtml(String menuSelection) {
        final String methodName = LCUtil.getCurrentMethodName();
        LOG.debug("entering {} with string = {}", menuSelection);
        reset("Reset from to_selectPurpose_xhtml, with : " + menuSelection);

        // 1. Resolve purpose from menu code
        enumeration.SelectionPurpose purpose = enumeration.SelectionPurpose.fromCode(menuSelection);
        LOG.debug("purpose resolved = {}", purpose);

        // 2. Open CDI selection context
        clubSelectionContext.open(purpose);

        // 3. Navigate to first page (delegated to enum)
        var navigation = purpose.navigationToFirst();
        LOG.debug("navigation resolved = {}", navigation);

        // 4. Special case: club creation flag
        if ("clubCreate".equals(menuSelection)) {
            appContext.getClub().setCreateModify(true);
        }

        return navigation;
    } // end method

    /**
     * Logout — stops audit, resets session, invalidates session.
     * Moved from PlayerController 2026-03-07 — logout is a session lifecycle action.
     */
    public String logout(String lgt) {
        final String methodName = LCUtil.getCurrentMethodName();
        LOG.debug("entering {} with parameter = {}", lgt);
        try {
            // 1. Fermer l'audit de la session courante — via auditId stocké en session (précis)
            HttpServletRequest request = (HttpServletRequest) FacesContext.getCurrentInstance()
                    .getExternalContext().getRequest();
            Integer sessionAuditId = (Integer) request.getSession().getAttribute("auditId");
            if (sessionAuditId != null) {
                Audit a = new Audit();
                a.setIdaudit(sessionAuditId);
                LOG.debug("stopping audit id={} for player={}", sessionAuditId,
                        appContext.getPlayer() != null ? appContext.getPlayer().getIdplayer() : "unknown");
                if (!updateAudit.stop(a)) {
                    LOG.warn("updateAudit.stop failed for auditId={}", sessionAuditId);
                }
            } else {
                LOG.warn("no auditId in session — audit not closed for lgt={}", lgt);
            }

            // 2. Reset CDI + invalidation session
            reset("from logout");
            FacesContext.getCurrentInstance().getExternalContext().invalidateSession();
            LOG.debug("session invalidated, lgt={}", lgt);

            // 3. Message + redirection (setKeepMessages(true) survit au redirect via flash JSF)
            return switch (lgt != null ? lgt : "") {
                case "from button Logout" -> {
                    showMessageInfo(utils.LCUtil.prepareMessageBean("logout.button.confirmation"));
                    yield "login.xhtml?faces-redirect=true";
                }
                default -> {
                    LOG.debug("logout lgt='{}' — redirect to login.xhtml", lgt);
                    yield "login.xhtml?faces-redirect=true";
                }
            };
        } catch (Exception e) {
            handleGenericException(e, methodName);
            return null;
        }
    } // end method

} // end class

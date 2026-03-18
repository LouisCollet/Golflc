package Controller.refact;

import entite.Audit;
import static interfaces.GolfInterface.ZDF_TIME;
import static interfaces.Log.LOG;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Inject;
import jakarta.inject.Named;

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

    public NavigationController() { }

    @PostConstruct
    public void init() {
        final String methodName = LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName);
        try {
            reset("from init in NavigationController");
            int timeout = FacesContext.getCurrentInstance().getExternalContext().getSessionMaxInactiveInterval();
            LOG.debug("NEW session started — charset = " + Charset.defaultCharset()
                    + ", session timeout = " + timeout + "s");
        } catch (Exception e) {
            String msg = "Exception in NavigationController.init() = " + e.getMessage();
            LOG.error(msg);
            LCUtil.showMessageFatal(msg);
        }
    } // end method

    @PreDestroy
    public void exit() {
        LOG.debug("NavigationController PreDestroy exit()");
    } // end method

    /**
     * Central reset — resets appContext, invalidates all caches, fires ResetEvent.
     * Each sub-controller observes ResetEvent and resets its own fields.
     */
    public void reset(String ini) {
        try {
            LOG.debug("starting NavigationController reset with: " + ini);
            appContext.reset();
            cacheInvalidator.invalidateAll();
            Controllers.LoggingUserController.setText("first start");

            if (appContext.getPlayer() != null) {
                appContext.getPlayer().clearDroppedPlayers();
                appContext.getPlayer().clearDraggedPlayers();
            }

            resetEvent.fire(new events.ResetEvent(ini));
            LOG.debug("ResetEvent fired for: " + ini);
        } catch (Exception ex) {
            LOG.error("Error in reset! " + ex);
            showMessageFatal("Exception reset = " + ex.toString());
        }
    } // end method

    /**
     * Logout — stops audit, resets session, invalidates session.
     * Moved from PlayerController 2026-03-07 — logout is a session lifecycle action.
     */
    public String logout(String lgt) {
        final String methodName = LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName + " for player = " + appContext.getPlayer().getIdplayer());
        LOG.debug("entering " + methodName + " with parameter = " + lgt);
        try {
            appContext.getPlayer().setShowMenu(true);
            if (appContext.getPlayer().getIdplayer() != null) {
                Audit a = new Audit();
                a.setAuditPlayerId(appContext.getPlayer().getIdplayer());
                a = findLastAudit.find(a);
                if (a != null) {
                    String msg = "ending an audit which started at : " + a.getAuditStartDate().format(ZDF_TIME);
                    LOG.debug(msg);
                    showMessageInfo(msg);
                    boolean ok = updateAudit.stop(a);
                }
            }
            reset("from logout");
            jakarta.faces.context.ExternalContext ec = FacesContext.getCurrentInstance().getExternalContext();
            LOG.debug("this session will be invalidated : " + ec.getSessionId(true));
            ec.invalidateSession();
            if (lgt != null) {
                if (lgt.equals("from button Logout")) {
                    String msg = "You asked a logout from the Logout button";
                    LOG.info(msg);
                    showMessageInfo(msg);
                    return "login.xhtml?faces-redirect=true";
                }
                if (lgt.equals("Inactive Interval from masterTemplate")) {
                    String msg = "Inactive Interval from masterTemplate - Time-out for inactivity from masterTemplate!";
                    LOG.debug(msg);
                    showMessageInfo(msg);
                    return "session_expired.xhtml?faces-redirect=true";
                } else {
                    LOG.debug("unknown logout message : " + lgt);
                    return null;
                }
            } else {
                LOG.debug("lgt is null " + lgt);
                return null;
            }
        } catch (Exception ex) {
            String msg = "Exception in " + methodName + " " + ex;
            LOG.error(msg);
            showMessageFatal(msg);
            return null;
        }
    } // end method

} // end class

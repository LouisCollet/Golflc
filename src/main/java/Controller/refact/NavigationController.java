package Controller.refact;

import static interfaces.Log.LOG;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.faces.context.ExternalContext;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import java.io.Serializable;
import java.nio.charset.Charset;
import context.ApplicationContext;
import jakarta.enterprise.context.SessionScoped;
import utils.LCUtil;

import static utils.LCUtil.showMessageFatal;

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
    @Inject private ExternalContext externalContext;
    @Inject private cache.CacheInvalidator cacheInvalidator;
    @Inject private jakarta.enterprise.event.Event<events.ResetEvent> resetEvent;

    public NavigationController() { }

    @PostConstruct
    public void init() {
        final String methodName = LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName);
        try {
            reset("from init in NavigationController");
            LOG.debug("NEW session started — charset = " + Charset.defaultCharset()
                    + ", session timeout = " + externalContext.getSessionMaxInactiveInterval() + "s");
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

} // end class

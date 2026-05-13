package startup;

import static exceptions.LCException.handleGenericException;
import static interfaces.Log.LOG;
import jakarta.annotation.PreDestroy;
import jakarta.annotation.Resource;
import jakarta.enterprise.concurrent.ManagedScheduledExecutorService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.Initialized;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

@ApplicationScoped
public class ExpirationScheduler implements Serializable {

    private static final long serialVersionUID = 1L;
    private static final int  EXPIRY_DAYS     = 7;
    private static final long FIRST_DELAY_SEC = 120;   // 2 min après le boot
    private static final long PERIOD_SEC      = 86400; // toutes les 24h

    @Resource(lookup = "java:comp/DefaultManagedScheduledExecutorService")
    private ManagedScheduledExecutorService executor;

    @Inject private delete.DeleteCart       deleteCartService;
    @Inject private delete.DeleteActivation deleteActivationService;

    private ScheduledFuture<?> future;

    public ExpirationScheduler() { }

    public void onStartup(@Observes @Initialized(ApplicationScoped.class) Object event) {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        try {
            future = executor.scheduleAtFixedRate(
                this::runDailyCleanup, FIRST_DELAY_SEC, PERIOD_SEC, TimeUnit.SECONDS);
            LOG.info("ExpirationScheduler started — period={}h expiry={}days", PERIOD_SEC / 3600, EXPIRY_DAYS);
        } catch (Exception e) {
            handleGenericException(e, methodName);
        }
    } // end method

    private void runDailyCleanup() {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        expireOldCarts();
        expireOldActivations();
    } // end method

    private void expireOldCarts() {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        try {
            LocalDateTime threshold = LocalDateTime.now().minusDays(EXPIRY_DAYS);
            int deletedCount = deleteCartService.deleteExpiredBefore(threshold);
            LOG.debug("deleted cart {}", deletedCount);
        } catch (Exception e) {
            handleGenericException(e, methodName);
        }
    } // end method

    private void expireOldActivations() {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        try {
            deleteActivationService.deleteExpired();
        } catch (Exception e) {
            handleGenericException(e, methodName);
        }
    } // end method

    @PreDestroy
    public void stop() {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        if (future != null) {
            future.cancel(false);
            LOG.info("ExpirationScheduler stopped");
        }
    } // end method

} // end class

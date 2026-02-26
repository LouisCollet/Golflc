package info_test;

import static interfaces.Log.LOG;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.concurrent.*;

@ApplicationScoped
public class InfoExecutor {

    private final ExecutorService pool = Executors.newFixedThreadPool(5);

    public InfoExecutor() {
        // constructeur CDI requis
    }

    public String execute(InfoProvider provider, long timeoutMs) {
        Future<String> future = pool.submit(() -> {
            try {
                return provider.get();
            } catch (Exception e) {
                LOG.error("{} provider error", provider.name(), e);
                return provider.name() + " - indisponible";
            }
        });

        try {
            return future.get(timeoutMs, TimeUnit.MILLISECONDS);
        } catch (TimeoutException e) {
            future.cancel(true);
            LOG.warn("{} timeout after {} ms", provider.name(), timeoutMs);
            return provider.name() + " - timeout";
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return provider.name() + " - interrompu";
        } catch (ExecutionException e) {
            LOG.error("{} execution failed", provider.name(), e.getCause());
            return provider.name() + " - erreur";
        }
    }

    public void shutdown() {
        pool.shutdownNow();
    }
}

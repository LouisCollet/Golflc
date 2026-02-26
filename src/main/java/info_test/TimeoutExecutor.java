package info_test;

import jakarta.annotation.Resource;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.concurrent.ManagedExecutorService;

import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Supplier;

@ApplicationScoped
public class TimeoutExecutor {

    private static final long DEFAULT_TIMEOUT = 2; // secondes

    @Resource
    private ManagedExecutorService executor;

    public <T> T execute(Supplier<T> task, T fallback, String label) {
        try {
            Future<T> future = executor.submit(task::get);
            return future.get(DEFAULT_TIMEOUT, TimeUnit.SECONDS);
        } catch (TimeoutException e) {
            return fallback;
        } catch (Exception e) {
            return fallback;
        }
    }
}

package wiki.utils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.*;

public class SimpleExecutorService {
    private final static Logger LOGGER = LogManager.getLogger(SimpleExecutorService.class);

    public static ExecutorService initExecutorService() {
        int cores = Runtime.getRuntime().availableProcessors();

        BlockingQueue<Runnable> blockingQueue = new ArrayBlockingQueue<>(100);
        RejectedExecutionHandler rejectedExecutionHandler = new ThreadPoolExecutor.CallerRunsPolicy();

        return new ThreadPoolExecutor(cores, cores,
                0L, TimeUnit.MILLISECONDS, blockingQueue, rejectedExecutionHandler);
    }

    public static void shutDownPool(ExecutorService executorService) {
        LOGGER.info("Shutting down thread pool and preparing to close process...");
        executorService.shutdown(); // Disable new tasks from being submitted
        try {
            // Wait a while for existing tasks to terminate
            if (!executorService.awaitTermination(5, TimeUnit.HOURS)) {
                executorService.shutdownNow(); // Cancel currently executing tasks
                // Wait a while for tasks to respond to being cancelled
                if (!executorService.awaitTermination(60, TimeUnit.SECONDS))
                    LOGGER.error("Pool did not terminate");
            }
        } catch (InterruptedException ie) {
            // (Re-)Cancel if current thread also interrupted
            executorService.shutdownNow();
            // Preserve interrupt status
            Thread.currentThread().interrupt();
        }
    }
}

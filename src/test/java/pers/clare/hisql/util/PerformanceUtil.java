package pers.clare.hisql.util;


import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;
import java.util.function.Function;

public class PerformanceUtil {

    public static double byCount(long max, Consumer<Long> consumer) throws Exception {
        return byCount(Runtime.getRuntime().availableProcessors(), max, consumer);
    }

    public static double byCount(int thread, long max, Consumer<Long> consumer) throws Exception {
        return byCondition(thread, (count) -> count <= max, consumer);
    }

    public static double byTime(long ms, Consumer<Long> consumer) throws Exception {
        return byTime(Runtime.getRuntime().availableProcessors(), ms, consumer);
    }

    public static double byTime(int thread, long ms, Consumer<Long> consumer) throws Exception {
        long endTime = System.currentTimeMillis() + ms;
        return byCondition(thread, (count) -> System.currentTimeMillis() < endTime, consumer);
    }

    public static double byCondition(Function<Long, Boolean> condition, Consumer<Long> consumer) throws Exception {
        return byCondition(Runtime.getRuntime().availableProcessors(), condition, consumer);
    }

    public static double byCondition(int thread, Function<Long, Boolean> condition, Consumer<Long> consumer) throws Exception {
        return byCondition(thread, condition, (index) -> {
            consumer.accept(index);
            return CompletableFuture.completedFuture(null);
        });
    }

    public static double byCount(long max, Function<Long, Future<Void>> consumer) throws Exception {
        return byCount(Runtime.getRuntime().availableProcessors(), max, consumer);
    }

    public static double byCount(int thread, long max, Function<Long, Future<Void>> consumer) throws Exception {
        return byCondition(thread, (count) -> count <= max, consumer);
    }

    public static double byTime(long ms, Function<Long, Future<Void>> consumer) throws Exception {
        return byTime(Runtime.getRuntime().availableProcessors(), ms, consumer);
    }

    public static double byTime(int thread, long ms, Function<Long, Future<Void>> consumer) throws Exception {
        long endTime = System.currentTimeMillis() + ms;
        return byCondition(thread, (count) -> System.currentTimeMillis() < endTime, consumer);
    }

    public static double byCondition(Function<Long, Boolean> condition, Function<Long, Future<Void>> consumer) throws Exception {
        return byCondition(Runtime.getRuntime().availableProcessors(), condition, consumer);
    }

    public static double byCondition(int thread, Function<Long, Boolean> condition, Function<Long, Future<Void>> consumer) throws Exception {
        AtomicLong counter = new AtomicLong();
        long startTime = System.currentTimeMillis();
        ScheduledFuture<?> printFuture = printFuture(counter, startTime, thread);
        Runnable shutdown = performance(thread, () -> {
            long index;
            while (condition.apply((index = counter.incrementAndGet()))) {
                try {
                    consumer.apply(index).get();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            counter.decrementAndGet();
            return null;
        });

        shutdown.run();
        printFuture.cancel(true);
        return println(counter, startTime, thread);
    }

    public static Runnable performance(int thread, Callable<Void> callable) throws InterruptedException, ExecutionException {
        ExecutorService executor = Executors.newFixedThreadPool(thread);
        List<Callable<Void>> tasks = new ArrayList<>();
        for (int i = 0; i < thread; i++) {
            tasks.add(callable);
        }
        for (Future<Void> future : executor.invokeAll(tasks)) {
            future.get();
        }
        return executor::shutdown;
    }

    private static ScheduledFuture<?> printFuture(AtomicLong counter, long startTime, int thread) {
        return Executors.newScheduledThreadPool(1)
                .scheduleAtFixedRate(() -> print(counter, startTime, thread), 0, 1, TimeUnit.SECONDS);
    }

    private static double print(AtomicLong counter, long startTime, int thread) {
        long count = counter.get();
        long time = System.currentTimeMillis() - startTime;
        double per = per(count, time);
        System.out.printf("concurrency: %d, count: %d, took time(ms): %d, %d/s, time(ms):  %f\r"
                , thread, count, time, rps(count, time), per);
        return per;
    }

    private static double println(AtomicLong counter, long startTime, int thread) {
        long count = counter.get();
        long time = System.currentTimeMillis() - startTime;
        double per = per(count, time);
        System.out.printf("concurrency: %d, count: %d, took time(ms): %d, %d/s, time(ms):  %f\n"
                , thread, count, time, rps(count, time), per);
        return per;
    }

    private static long rps(long count, long ms) {
        ms = ms == 0 ? 1 : ms;
        return count * 1000 / ms;
    }


    private static double per(long count, long ms) {
        if (count == 0) return 0;
        return (double) ms * 1000 / count;
    }


}

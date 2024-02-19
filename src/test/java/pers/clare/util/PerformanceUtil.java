package pers.clare.util;


import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;
import java.util.function.Function;

public class PerformanceUtil {

    public static double byCount(String tag, long max, Consumer<Long> consumer) throws Exception {
        return byCount(tag, Runtime.getRuntime().availableProcessors(), max, consumer);
    }

    public static double byCount(String tag, int thread, long max, Consumer<Long> consumer) throws Exception {
        return byCondition(tag, thread, (count) -> count <= max, consumer);
    }

    public static double byTime(String tag, long ms, Consumer<Long> consumer) throws Exception {
        return byTime(tag, Runtime.getRuntime().availableProcessors(), ms, consumer);
    }

    public static double byTime(String tag, int thread, long ms, Consumer<Long> consumer) throws Exception {
        long endTime = System.currentTimeMillis() + ms;
        return byCondition(tag, thread, (count) -> System.currentTimeMillis() < endTime, consumer);
    }

    public static double byCondition(String tag, Function<Long, Boolean> condition, Consumer<Long> consumer) throws Exception {
        return byCondition(tag, Runtime.getRuntime().availableProcessors(), condition, consumer);
    }

    public static double byCondition(String tag, int thread, Function<Long, Boolean> condition, Consumer<Long> consumer) throws Exception {
        return byCondition(tag, thread, condition, (index) -> {
            consumer.accept(index);
            return CompletableFuture.completedFuture(null);
        });
    }

    public static double byCount(String tag, long max, Function<Long, Future<Void>> consumer) throws Exception {
        return byCount(tag, Runtime.getRuntime().availableProcessors(), max, consumer);
    }

    public static double byCount(String tag, int thread, long max, Function<Long, Future<Void>> consumer) throws Exception {
        return byCondition(tag, thread, (count) -> count <= max, consumer);
    }

    public static double byTime(String tag, long ms, Function<Long, Future<Void>> consumer) throws Exception {
        return byTime(tag, Runtime.getRuntime().availableProcessors(), ms, consumer);
    }

    public static double byTime(String tag, int thread, long ms, Function<Long, Future<Void>> consumer) throws Exception {
        long endTime = System.currentTimeMillis() + ms;
        return byCondition(tag, thread, (count) -> System.currentTimeMillis() < endTime, consumer);
    }

    public static double byCondition(String tag, Function<Long, Boolean> condition, Function<Long, Future<Void>> consumer) throws Exception {
        return byCondition(tag, Runtime.getRuntime().availableProcessors(), condition, consumer);
    }

    public static double byCondition(String tag, int thread, Function<Long, Boolean> condition, Function<Long, Future<Void>> consumer) throws Exception {
        AtomicLong counter = new AtomicLong();

        long startTime = System.currentTimeMillis();

        ScheduledExecutorService executor = Executors.newScheduledThreadPool(thread + 1);
        ScheduledFuture<?> printFuture = executor.scheduleAtFixedRate(() -> print(tag, counter, startTime, thread), 0, 1, TimeUnit.SECONDS);

        List<Callable<Void>> tasks = new ArrayList<>();
        for (int i = 0; i < thread; i++) {
            tasks.add(() -> {
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
        }
        for (Future<Void> future : executor.invokeAll(tasks)) {
            future.get();
        }
        printFuture.cancel(true);
        executor.shutdown();
        return println(tag, counter, startTime, thread);
    }

    private static double print(String tag, AtomicLong counter, long startTime, int thread) {
        long count = counter.get();
        long time = System.currentTimeMillis() - startTime;
        double per = per(count, time);
        System.out.printf("%s concurrency: %d, count: %d, took time(ms): %d, %d/s, time(ms):  %f\r"
                , tag, thread, count, time, rps(count, time), per);
        return per;
    }

    private static double println(String tag, AtomicLong counter, long startTime, int thread) {
        long count = counter.get();
        long time = System.currentTimeMillis() - startTime;
        double per = per(count, time);
        System.out.printf("%s concurrency: %d, count: %d, took time(ms): %d, %d/s, time(ms):  %f\n"
                , tag, thread, count, time, rps(count, time), per);
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

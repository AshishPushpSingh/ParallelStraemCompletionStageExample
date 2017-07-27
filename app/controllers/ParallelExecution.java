package controllers;

import play.Logger;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

/**
 * Created by Ashish Pushp Singh on 27/7/17.
 */

public class ParallelExecution {


    private static final String PROCESSED_TIME_MESSAGE = "Processed {} tasks in {} seconds\n";
    private static final String RESULT_MESSAGE = "Result is :: {}";
    private static final String SECONDS = "sec";
    private static final long TO_SECOND = 1_000_000_000;

     static String runSequentially(List<Work> works) {
        long start = System.nanoTime();
        List<Integer> result = works.stream()
                .map(Work::calculate)
                .collect(Collectors.toList());
        long duration = (System.nanoTime() - start) / TO_SECOND;
        Logger.debug("runSequentially " +PROCESSED_TIME_MESSAGE, works.size(), duration); //Processed 10 tasks in 10003 millis
        Logger.debug(RESULT_MESSAGE, result);

        return "runSequentially ::: " + duration + SECONDS;
    }

    static String useParallelStream(List<Work> works) {
        long start = System.nanoTime();
        List<Integer> result = works.parallelStream()
                .map(Work::calculate)
                .collect(Collectors.toList());
        long duration = (System.nanoTime() - start) / TO_SECOND;
        Logger.debug("useParallelStream " + PROCESSED_TIME_MESSAGE, works.size(), duration); //Processed 10 tasks in 3002 millis
        Logger.debug(RESULT_MESSAGE, result);

        return "useParallelStream ::: " + duration + SECONDS;
    }

     static String useCompletableFuture(List<Work> works) {
        long start = System.nanoTime();
        List<CompletableFuture<Integer>> futures =
                works.stream()
                        .map(aTask -> CompletableFuture.supplyAsync(aTask::calculate))
                        .collect(Collectors.toList());

        List<Integer> result =
                futures.stream()
                        .map(CompletableFuture::join)
                        .collect(Collectors.toList());
        long duration = (System.nanoTime() - start) / TO_SECOND;
        Logger.debug("useCompletableFuture " + PROCESSED_TIME_MESSAGE, works.size(), duration); //Processed 10 tasks in 4003 millis
        Logger.debug(RESULT_MESSAGE, result);

        return "useCompletableFuture ::: " + duration + SECONDS;
    }

     static String useCompletableFutureWithExecutor(List<Work> works) {
        long start = System.nanoTime();
        ExecutorService executor = Executors.newFixedThreadPool(Math.min(works.size(), 10));
        List<CompletableFuture<Integer>> futures =
                works.stream()
                        .map(t -> CompletableFuture.supplyAsync(t::calculate, executor))
                        .collect(Collectors.toList());

        List<Integer> result =
                futures.stream()
                        .map(CompletableFuture::join)
                        .collect(Collectors.toList());
        long duration = (System.nanoTime() - start) / TO_SECOND;
        Logger.debug("useCompletableFutureWithExecutor " + PROCESSED_TIME_MESSAGE, works.size(), duration); //Processed 10 tasks in 1014 millis
        Logger.debug(RESULT_MESSAGE, result);
        executor.shutdown();

        return "useCompletableFutureWithExecutor ::: " + duration + SECONDS;
    }

}

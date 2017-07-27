package controllers;

import play.libs.Json;
import play.mvc.*;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.IntStream;

import java.util.stream.Collectors;

import static controllers.ParallelExecution.*;

/**
 * Created by Ashish Pushp Singh on 27/7/17.
 */

public class HomeController extends Controller {

    public Result index() {
        return ok("Your new application is ready.");
    }

    public Result testSpeed() {

        List<String> taskSpeedlist = new CopyOnWriteArrayList<>();
        List<Work> workList = IntStream.range(0, 10)
                .mapToObj(i -> new Work(1))
                .collect(Collectors.toList());

        taskSpeedlist.add(runSequentially(workList));
        taskSpeedlist.add(useParallelStream(workList));
        taskSpeedlist.add(useCompletableFuture(workList));
        taskSpeedlist.add(useCompletableFutureWithExecutor(workList));

        return ok(Json.toJson(taskSpeedlist)).as("application/Json");
    }


}

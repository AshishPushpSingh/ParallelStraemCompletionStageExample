package controllers;

import com.google.inject.Inject;
import play.Logger;
import play.libs.Json;
import play.libs.concurrent.Futures;
import play.libs.ws.WSClient;
import play.libs.ws.WSResponse;
import play.mvc.Controller;
import play.mvc.Result;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.CompletionStage;
import java.util.stream.Collectors;

/**
 * Created by Ashish Pushp Singh on 27/7/17.
 */

public class ErrorCompletionStage extends Controller {


    @Inject
    private WSClient wsClient;

    private String func1() throws Exception {
        throw new FileNotFoundException("Not found");
    }

    private String func2() throws IOException {
        throw new IOException("IOException");
    }

    // Example : Use exceptionally method to raise and hanle list of Promises (CompletionStage).
    public CompletionStage<Result> exceptionallyExample() {

        List<CompletableFuture<String>> listCompletionStage =
                new ArrayList<>();

        CompletableFuture<String> res1 = CompletableFuture.supplyAsync(() -> {
            try {
                return func2();
            } catch (Exception e) {
                throw new CompletionException(e);
            }
        }).thenApplyAsync(s -> s + "added");

        CompletableFuture<String> res2 = CompletableFuture.supplyAsync(() -> {
            try {
                return func1();
            } catch (Exception e) {
                throw new CompletionException(e);
            }
        }).thenApply(s -> s + "added");

        listCompletionStage.add(res1);
        listCompletionStage.add(res2);

        //One of the ways to convert list of CompletionStage to CompletableFuture of List.
        CompletableFuture[] cfs = listCompletionStage.toArray(new CompletableFuture[listCompletionStage.size()]);
        CompletableFuture<List<String>> futureOIResponses = CompletableFuture.allOf(cfs)
                .thenApplyAsync(v -> listCompletionStage.stream().map(response -> {
                    try {
                        return response.get();
                    } catch (Exception e) {
                        return null;
                    }
                }).filter(Objects::nonNull).collect(Collectors.toList()));


        return Futures.sequence(listCompletionStage)
                .thenApplyAsync(strings -> ok(Json.toJson(strings)))
                .exceptionally(throwable -> internalServerError(throwable.getMessage()));
    }


    // Example : Use Future.sequence to hanle list of completionStage Objects.
    public CompletionStage<Result> fileUpload(){

        List<CompletionStage<WSResponse>> listCompletionStage =
                new ArrayList<>();

        String fileUploadUrl = "http://localhost:9000/simple/fileupload";

        CompletionStage<WSResponse> response1 = wsClient.url(fileUploadUrl)
                .post(new File("/Users/xyz/Desktop/FilesDump/file1.pdf"));

        listCompletionStage.add(response1);

        CompletionStage<WSResponse> response2 = wsClient.url(fileUploadUrl)
                .post(new File("/Users/xyz/Desktop/FilesDump/file2.pdf"));

        listCompletionStage.add(response2);

        return Futures.sequence(listCompletionStage)
                .thenApplyAsync(wsResponses -> {
                    Logger.info("In sequence ");
                    ArrayList<String> arrayList = new ArrayList<>();
                    wsResponses
                            .stream()
                            .map(WSResponse::getBody)
                            .forEach(arrayList::add);
                    return ok(Json.toJson(arrayList));
                });
    }

}

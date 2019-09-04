package com.thecodinginterface.restconsumer;

import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse.BodyHandlers;
import java.util.ResourceBundle;
import java.util.StringJoiner;
import java.util.stream.Collectors;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.web.WebView;
import javafx.util.Pair;
import javafx.util.StringConverter;

public class Controller implements Initializable {

    @FXML
    private Label urlLabel;

    @FXML
    private Label requestVerbLabel;

    @FXML
    private Label responseCodeLabel;

    @FXML
    private ComboBox<Pair<String, URI>> requestTypeComboBox;

    @FXML
    private TextArea requestHeaderTextArea;

    @FXML
    private TextArea requestBodyTextArea;

    @FXML
    private TextArea responseHeadersTextArea;

    @FXML
    private TextArea responseBodyTextArea;

    @FXML
    private WebView webView;

    HttpClient client;

    ObservableList<Pair<String, URI>> requestPairs;

    Gson gson;

    @FXML
    void handleChangeRequest(ActionEvent event) {
        var requestPair = requestTypeComboBox.getValue();
        if (requestPair != null) {
            urlLabel.setText(requestPair.getKey() + ": " + requestPair.getValue());
        }
    }

    @FXML
    void handleFetch(ActionEvent event) {
        var httpRequestPair = requestTypeComboBox.getValue();
        
        try {
            var uri = httpRequestPair.getValue();
            switch(httpRequestPair.getKey()) {
                case "GET-Sync":
                    doGET(uri);
                    break;
                case "GET-Async":
                    doGETAsync(uri);
                    break;
                case "POST-Sync":
                    doPOST(uri);
                    break;
                case "POST-Async":
                    doPOSTAsync(uri);
                    break;
                case "PUT-Sync":
                    doPUT(uri);
                    break;
                case "PUT-Async":
                    doPUTAsync(uri);
                    break;
                case "DELETE-Sync":
                    doDELETE(uri);
                    break;
                case "DELETE-Async":
                    doDELETEAsync(uri);
                    break;
            }
            var url = "https://thecodinginterface.com/blog/java-http-client/#" + httpRequestPair.getKey();
            webView.getEngine().load(url);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        gson = new GsonBuilder().setPrettyPrinting().create();
        client = HttpClient.newHttpClient();
        requestPairs = FXCollections.observableArrayList(
            new Pair<String, URI>("GET-Sync", URI.create("https://postman-echo.com/get?name=Adam&profession=Software")),
            new Pair<String, URI>("GET-Async", URI.create("https://postman-echo.com/get?name=Adam&profession=Software")),
            new Pair<String, URI>("POST-Sync", URI.create("https://postman-echo.com/post")),
            new Pair<String, URI>("POST-Async", URI.create("https://postman-echo.com/post")),
            new Pair<String, URI>("PUT-Sync", URI.create("https://postman-echo.com/put")),
            new Pair<String, URI>("PUT-Async", URI.create("https://postman-echo.com/put")),
            new Pair<String, URI>("DELETE-Sync", URI.create("https://postman-echo.com/delete?name=Adam")),
            new Pair<String, URI>("DELETE-Async", URI.create("https://postman-echo.com/delete?name=Adam"))
        );

        requestTypeComboBox.setItems(requestPairs);

        requestTypeComboBox.setConverter(new StringConverter<Pair<String, URI>>() {

            @Override
            public String toString(Pair<String, URI> object) {
                if (object == null) {
                    return "";
                }
                return object.getKey();
            }

            @Override
            public Pair<String, URI> fromString(String string) {
              return null;
            }
        });
    }

    void doGET(URI uri) throws Exception {
        // the default builder http method is GET so calling 
        // .GET() on the builder is not necessary
        var request = HttpRequest.newBuilder(uri)
                                    .header("Accept", "application/json")
                                    .build();
        populateRequestFields(request, null);

        // send() is a blocking synchronous call
        HttpResponse<String> response = client.send(request, BodyHandlers.ofString());
        populateResponseFields(response);
    }

    void doGETAsync(URI uri) throws Exception {
        var request = HttpRequest.newBuilder(uri)
                                  .header("Accept", "application/json")
                                  .build();
        populateRequestFields(request, null);

        // here I chain the sendAsync call with a Consumer functional interface
        // CompetionStage call passing it a functional reference
        client.sendAsync(request, BodyHandlers.ofString())
              .thenAccept(this::populateResponseFields);
    }
 
    void doPOST(URI uri) throws Exception {
        var person = new Person("Adam", "Software");
        var bodyJson = gson.toJson(person);
        var request = HttpRequest.newBuilder(uri)
                                  .header("Accept", "application/json")
                                  .header("Content-Type", "application/json")
                                  .POST(BodyPublishers.ofString(bodyJson))
                                  .build();
        populateRequestFields(request, bodyJson);
        HttpResponse<String> response = client.send(request, BodyHandlers.ofString());
        populateResponseFields(response);
    }

    void doPOSTAsync(URI uri) throws Exception {
        var person = new Person("Adam", "Software");
        var bodyJson = gson.toJson(person);
        var request = HttpRequest.newBuilder(uri)
                                .header("Accept", "application/json")
                                .header("Content-Type", "application/json")
                                .POST(BodyPublishers.ofString(bodyJson))
                                .build();
        populateRequestFields(request, bodyJson);
        client.sendAsync(request, BodyHandlers.ofString())
              .thenAccept(this::populateResponseFields);
    }


    void doPUT(URI uri) throws Exception {
        var person = new Person("Adam", "Software");
        var bodyJson = gson.toJson(person);
        var request = HttpRequest.newBuilder(uri)
                                .header("Accept", "application/json")
                                .header("Content-Type", "application/json")
                                .PUT(BodyPublishers.ofString(bodyJson))
                                .build();
        populateRequestFields(request, bodyJson);
        HttpResponse<String> response = client.send(request, BodyHandlers.ofString());
        populateResponseFields(response);
    }

    void doPUTAsync(URI uri) throws Exception {
        var person = new Person("Adam", "Software");
        var bodyJson = gson.toJson(person);
        var request = HttpRequest.newBuilder(uri)
                                .header("Accept", "application/json")
                                .header("Content-Type", "application/json")
                                .PUT(BodyPublishers.ofString(bodyJson))
                                .build();
        populateRequestFields(request, bodyJson);
        client.sendAsync(request, BodyHandlers.ofString())
              .thenAccept(this::populateResponseFields);
    }

    void doDELETE(URI uri) throws Exception {
        var request = HttpRequest.newBuilder(uri)
                                .header("Accept", "application/json")
                                .DELETE()
                                .build();
        populateRequestFields(request, null);
        HttpResponse<String> response = client.send(request, BodyHandlers.ofString());
        populateResponseFields(response);
    }

    void doDELETEAsync(URI uri) throws Exception {
        var request = HttpRequest.newBuilder(uri)
                                .header("Accept", "application/json")
                                .DELETE()
                                .build();
        populateRequestFields(request, null);
        client.sendAsync(request, BodyHandlers.ofString())
              .thenAccept(this::populateResponseFields);
    }

    void populateRequestFields(HttpRequest request, String jsonBody) {
        var sj = new StringJoiner("\n");
        request.headers().map().entrySet().forEach(entry -> {
            var header = entry.getKey() + " " + entry.getValue().stream().collect(Collectors.joining(" "));
            sj.add(header);
        });
        requestHeaderTextArea.setText(sj.toString());
        requestVerbLabel.setText(request.method());
        requestBodyTextArea.setText(jsonBody == null ? "" : jsonBody);
    }

    void populateResponseFields(HttpResponse<String> response) {
        Platform.runLater(() -> {
            var sj = new StringJoiner("\n");
            response.headers().map().entrySet().forEach(entry -> {
                var header = entry.getKey() + " " + entry.getValue().stream().collect(Collectors.joining(" "));
                sj.add(header);
            });
    
            responseHeadersTextArea.setText(sj.toString());
            responseCodeLabel.setText(Integer.toString(response.statusCode()));

            String responseBody = null;
            if (response.statusCode() == 200) {
                switch(response.request().method()) {
                    case "GET":
                    case "DELETE":
                        responseBody = gson.toJson(gson.fromJson(response.body(), PersonGetDeleteResponse.class));
                        break;
                    case "POST":
                    case "PUT":
                        responseBody = gson.toJson(gson.fromJson(response.body(), PersonPutPostResponse.class));
                        break;
                    default:
                        responseBody = response.body();
                }
            }
            responseBodyTextArea.setText(responseBody);
        });
    }
}

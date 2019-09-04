In this article I demonstrate using the new HTTP Client API introduced in Java 11 to consume RESTful web services in conjunction with the popular Google Gson serialization library. As a demonstration aid I utilize a JavaFX desktop app for inspecting and displaying key HTTP Request / Response data accessible via the HTTP Client API.

# RESTful Consumption using the Java 11+ HTTP Client API and Gson

### Introduction

In this article I demonstrate using the new [HTTP Client API](https://docs.oracle.com/en/java/javase/11/docs/api/java.net.http/java/net/http/package-summary.html) introduced in Java 11 to consume RESTful web services in conjunction with the popular Google Gson serialization library. As a demonstration aid I utilize a JavaFX desktop app for inspecting and displaying key HTTP Request / Response data accessible via the HTTP Client API.

#### Contents

 * Introducing the Key HTTP Client API Classes
 * JavaFX Http Request / Response Inspector App
 * Synchronous GET Request
 * Asynchronous GET Request
 * Synchronous POST Request
 * Asynchronous POST Request
 * Synchronous PUT Request
 * Asynchronous PUT Request
 * Synchonous DELETE Request
 * Asynchonous DELETE Request

### Introducing the Key HTTP Client API Classes
 
The new Http Client API introduced in Java 11 lives in the [java.net.http package](https://docs.oracle.com/en/java/javase/11/docs/api/java.net.http/java/net/http/package-summary.html) and is a revamped approach to handling HTTP based network communication. The Http Client API has an intuitive and modern API allowing for both synchronous and asynchronous code flows along with sensible defaults like first trying HTTP 2 then failing back to HTTP 1.1. 
 
These are the key types to be familar with in the Http Client API:

* HttpClient is the moderator which wraps all requests and responses 
* HttpRequest represents the Http Request being made and send by HttpClient
* HttpResponse represents the Http Response to a Http Request

There is a generalized workflow that occurs when using the Http Client API for the familar request / response cyclical nature of HTTP network calls that is as follows.

1) Build a HttpClient instance

```
HttpClient client = HttpClient.newHttpClient();
```

2) Build a HttpRequest instance

```
HttpRequest request = HttpRequest.newBuilder(URI.create("http://example.com"))
										.build();
```

3) Use the HttpClient instance to initiate sending the request to the target endpoint for the http request

```
HttpResponse<String> response = client.send(request, BodyHandlers.ofString());
```

4) Handle the response as it is returned

```
System.out.println("Hey look, an http response " + response.body());
```

In the examples for this article I am reusing the same HttpClient instance which I construct with the default creational method of HttpClient#newHttpClient as shown in step one above. However, the HttpClient utilizes the powerful builder pattern for creating HttpClient instances that require special configuration such as the following excerpt from the OpenJDK docs that introduces the [Http Client API](https://openjdk.java.net/groups/net/httpclient/intro.html).

```
HttpClient client = HttpClient.newBuilder()
      .version(Version.HTTP_2)
      .followRedirects(Redirect.SAME_PROTOCOL)
      .proxy(ProxySelector.of(new InetSocketAddress("www-proxy.com", 8080)))
      .authenticator(Authenticator.getDefault())
      .build();
```

The HttpRequest also utilizes a builder to configure and construct a reusable instance, again, here is the example from the OpenJDK docs.

```
HttpRequest request = HttpRequest.newBuilder()
      .uri(URI.create("http://openjdk.java.net/"))
      .timeout(Duration.ofMinutes(1))
      .header("Content-Type", "application/json")
      .POST(BodyPublishers.ofFile(Paths.get("file.json")))
      .build()
```

You will see several other examples of constructing a HttpRequest throughout this article.
 
### JavaFX Http Request / Response Inspector App
 
To aid in demonstrating the Http Client API I have built a JavaFX desktop app that allows the user to interactively generate common HTTP requests such as GET, POST, PUT, and DELETE directed to the freely available [Postman Echo service](https://docs.postman-echo.com/?version=latest) to replicate calling a real life RESTful endpoint. The Request / Response tab of the demo app is then populated with request / response headers and body data using the various Http Client API classes.  The second tab of the app is a simple WebView that shows the code associated with the last request as it appears in this blog post.
 
 *** INSERT PICTURE OF JAVAFX APPLICATION ***
 
The key understanding how this JavaFX application works is that the ComboBox dropdown is backed by the following list which uses a Pair<String, URI> to display the type of request and the Postman Echo server endpoint to be called.

```
FXCollections.observableArrayList(
    new Pair<String, URI>("GET Sync", URI.create("https://postman-echo.com/get?name=Adam&profession=Software")),
    new Pair<String, URI>("GET Async", URI.create("https://postman-echo.com/get?name=Adam&profession=Software")),
    new Pair<String, URI>("POST Sync", URI.create("https://postman-echo.com/post")),
    new Pair<String, URI>("POST Async", URI.create("https://postman-echo.com/post")),
    new Pair<String, URI>("PUT Sync", URI.create("https://postman-echo.com/put")),
    new Pair<String, URI>("PUT Async", URI.create("https://postman-echo.com/put")),
    new Pair<String, URI>("DELETE Sync", URI.create("https://postman-echo.com/delete?name=Adam")),
    new Pair<String, URI>("DELETE Async", URI.create("https://postman-echo.com/delete?name=Adam"))
);
```

Then when the fetch button is clicked a method in the Controller.java source file matching the selected request is called. These methods then utilize the HttpClient instance to fire off the HTTP request and handle the response populating the headers and body TextArea controls of the application.

### Synchronous GET Request

When a user of the demo JavaFX app selects GET-Sync as the request type and clicks fetch the below, doGet(URI), method is called and begins by constructing a HttpRequest instance via its builder which by default is of type GET. You will notice I also set the Accept header to application/json indicating to the requested endpoint that I expect JSON returned.

```
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
```

After constructing the HttpClient instance I make a call to populateRequestFields(HttpRequest, String) which displays the headers retrieved from HttpRequest#headers method and if the second string parameter is not null then that represents a body being sent in the request but, in the case of a GET request there is no body.

When a GET request is sent to https://postman-echo.com/get?name=Adam&profession=Software the service echos back a JSON response as seen below.  This method calls the send(HttpRequest, HttpBodyHandler<String>) method which initiates a blocking synchronous request to the url just mentioned. The second parameter to the various send / sendAsync method signatures are known as HttpBodySubscribers which process the response bodies returned from the request / response pair.

```
{
	"args": {
		"name": "Adam",
		"profession": "Software"
	},
	"headers": {
		"x-forwarded-proto": "https",
		"host": "postman-echo.com",
		"accept": "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3",
		"accept-encoding": "gzip, deflate, br",
		"accept-language": "en-US,en;q=0.9",
		"cookie": "_ga=GA1.2.147817848.1567195449; _gid=GA1.2.1857290537.1567534467",
		"sec-fetch-mode": "navigate",
		"sec-fetch-site": "none",
		"upgrade-insecure-requests": "1",
		"user-agent": "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_14_6) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/76.0.3809.100 Safari/537.36",
		"x-forwarded-port": "443"
	},
	"url": "https://postman-echo.com/get?name=Adam&profession=Software"
}
```

The doGET(URI) method ends by calling populateResponseFields(HttpResponse<String>) which parses the response body's JSON into the below PersonGetDeleteResponse and Person classes with the help of the Gson library before displaying the pretty printed JSON to the UI. The populateResponseFields(HttpResponse<String>) method also constructs a String representation of the response headers and displays them  as well.

PersonGetDeleteResponse.java

```
package com.thecodinginterface.restconsumer;

import com.google.gson.annotations.SerializedName;

class PersonGetDeleteResponse {
    @SerializedName("args")
    private Person person;

    PersonGetDeleteResponse() {}

    PersonGetDeleteResponse(Person person) {
        this.person = person;
    }

    void setPerson(Person person) {
        this.person = person;
    }

    Person getPerson() {
        return person;
    }
}
```

Person.java

```
package com.thecodinginterface.restconsumer;

class Person {
    private String name;
    private String profession;

    public Person() {}

    public Person(String name, String profession) {
        this.name = name;
        this.profession = profession;
    }

    void setName(String name) {
        this.name = name;
    }

    String getName() {
        return name;
    }

    void setProfession(String profession) {
        this.profession = profession;
    }

    String getProfession() {
        return profession;
    }
}
```

And here is the result in the JavaFX app's Request / Response tab.

*** request / response TAB ***

### Asynchronous GET Request

Asynchronous requests differ in that instead of waiting until the full HTTP response is complete then returning a HttpResponse<String> object they immediately return a CompletableFuture<HttpResponse> object which implements the reactive streams asynchronous paradigm of the [java.util.concurrent.Flow API](https://docs.oracle.com/javase/9/docs/api/java/util/concurrent/Flow.html). With this approach data is treated as a stream and operations are chained to process the data utilizing the CompletionStage pattern as described in the Oracle community article [CompletableFuture for Asynchronous Programming in Java 8](https://community.oracle.com/docs/DOC-995305).

So, when a user selects GET-Async as a request type then clicks Fetch the same url is requested as the synchronous GET request but, the programming flow is a little different inside the doGETAsync(URI uri) method as shown below.

```
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
```

As you can see I chain a CompletionStage consumer functional interface call of thenAccept(...) passing it a method reference to the populateResponseFields(HttpResponse<String>) method seen previously. From the perspective of the JavaFX UI the two requests are indistinguishable.

### Synchronous POST Request
 
Sending a POST request requires constructing the HttpRequest object with a call to the builder's POST(BodyPublisher) method with the BodyPublisher configured with the data you wish to have posted to the http endpoint which in this example is https://postman-echo.com/post. Note that I also explicitly configure the HTTP Request headers to specify that I'm sending Content-Type data of JSON in addition to the previously mentioned Accept header indicating I want JSON returned.

```
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
```

The response from the server looks identical to that of the GET request except that the returned JSON representing the POST response data is matched up to the JSON data key instead of the args key as seen in previously in the GET method and the DELETE request that concludes this article. To marshal the JSON response I use the following PersonPutPostResponse POJO class in conjunction with the Person class and the Gson library.

```
package com.thecodinginterface.restconsumer;

import com.google.gson.annotations.SerializedName;

class PersonPutPostResponse {
    @SerializedName("data")
    private Person person;

    PersonPutPostResponse() {}

    PersonPutPostResponse(Person person) {
        this.person = person;
    }

    void setPerson(Person person) {
        this.person = person;
    }

    Person getPerson() {
        return person;
    }
}
```

And here is the result in the JavaFX app's Request / Response tab.

*** request / response TAB ***

### Asynchronous POST Request

The Async POST is a natural extension of the previous examples which again utilizes the POST(...) method to construct the HttpRequest instance. Then I again use the sendAsync(HttpRequest, HttpBodyHandler) from the client instance which is then chained with a CompletionStage consumer to handle the response.

```
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
```

### Synchronous PUT Request
 
The PUT request essentially follows the same mechanics as the synchronous POST request except that the HttpRequest object is constructed with a call to PUT(BodyPublisher) in place of the POST(BodyPublisher) saw previously along with the url endpoint is https://postman-echo.com/put.

```
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
```

And here is the result in the JavaFX app's Request / Response tab.

*** request / response TAB ***

### Asynchronous PUT Request
 
Similarly the Async version of the PUT request is essentially the same as the async POST with the exception that PUT(BodyPublisher) is called during HttpRequest construction. 

```
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
```

### Synchonous DELETE Request

Performing a HTTP DELETE request is very similar to a GET request with the one obvious difference in that instead of requesting to have data returned from the url endpoint you are requesting to have data removed from that endpoint. To do this you simply construct the HttpRequest instance using the DELETE() builder method then your off to the races as long as the correct endpoint is specified. For this example I am simulating a DELETE request for the url https://postman-echo.com/delete?name=Adam of a resource with name equal to Adam.

```
void doDELETE(URI uri) throws Exception {
    var request = HttpRequest.newBuilder(uri)
                            .header("Accept", "application/json")
                            .DELETE()
                            .build();
    populateRequestFields(request, null);
    HttpResponse<String> response = client.send(request, BodyHandlers.ofString());
    populateResponseFields(response);
}
```

And here is the result in the JavaFX app's Request / Response tab.

*** request / response TAB ***
 
### Asynchonous DELETE Request

And for completeness here is the asynchronous version of the DELETE request which should be no suprise at this point.

```
void doDELETEAsync(URI uri) throws Exception {
    var request = HttpRequest.newBuilder(uri)
                            .header("Accept", "application/json")
                            .DELETE()
                            .build();
    populateRequestFields(request, null);
    client.sendAsync(request, BodyHandlers.ofString())
          .thenAccept(this::populateResponseFields);
}
```

### Conclusion

In this article I have discussed the merits and design philosophy of the new Java 11 HTTP Client API as well as provided several examples of how to perform the common GET, POST, PUT, and DELETE HTTP request methods. To simulate real life RESTful endpoints I used  the Postman Echo service and provided a JavaFX based UI for inspecting the contents of requests and responses.
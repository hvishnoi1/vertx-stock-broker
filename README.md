# Vert.x web

Vert.x web supports 2 packages for handling the server and client logic.

- Server
  - Built on top of **Netty**, a reactive HTTP web server
  - Non-blocking and asynchronous execution
  - Reactive HTTP Request processing
  - Out of box support for JSON with Jackson
  - Support for Web Sockets and HTTP/2
- Client
  - Asynchronous HTTP and HTTP/2 clients
  - JSON encoding/decoding support out of the box
  - Support for request/response streaming
  - Supports RxJava 2 API for reactive programming

For using Vetrt.x web, include **Vert.x Web** dependancy.

## Handling errors

A global exception handler can be configured on the `Vertx` instance using
`vertx.exceptionHandler(error->{...})`

We can also provide handlers taking an **asynchronous result** as input passed
in the `deployVerticle` method as `vertx.deployVerticle(new Verticle(), ar-> {...})`

Refer `main` method in [MainVerticle.java](src/main/java/com/example/broker/MainVerticle.java)

## Router

A router can be created using `Router.router(vertx)` where `vertx` is the Vertx
instance. By calling different methods such as `get(...)`, `post(...)`, `put(...)`,
`delete(...)` on the router object,we can define various endpoints and their
respective handlers. These handlers are then respectively called when a client
hits the specified endpoint.

The handler gets an instance of `RoutingContext` as parameter which gives access to
request related information. This can be done by using a lambda callback function
or by using an object of a **class implementing the `Handler<RoutingContext>
interface` in the `io.vertx.core.Handler` package**

Refer
[GetQuotesHandler.java](src/main/java/com/example/broker/quotes/GetQuoteHandler.java)
and [QuotesRestApi.java](src/main/java/com/example/broker/quotes/QuotesRestApi.java)

A failure handler can be registered on our **Router** as
`router.route().failuerHandler(routingContext -> {...})`.

```java
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;

class MainVerticle extends AbstractVerticle {
  // ...
  @Override
  public void start(Promise<Void> startPromise) throws Exception {
    final Router router = Router.router(vertx);

    router.route().failureHandler(routingContext -> {
      if (routingContext.response().ended()) { // Client closed the connection
        return;
      }
      // ...
      routingContext.response()
        .setStatusCode(500)
        .end(new JsonObject("Error").toBuffer());
    });

    router.get("/").handler(routingContext -> {
      final JsonArray response = new JsonArray();
      // ...
      routingContext.response().end(response.toBuffer());
    });

    vertx.createHttpServer()
      .requestHandler(restApi)
      .errorHandler(err -> {
        // ...
      })
      .listen(PORT, http -> {
        // ...
      });
  }
}
```

See [RestApiVerticle.java](src/main/java/com/example/broker/RestApiVerticle.java)

## Testing

Vert.x APIs can be tested using the Vertx's HTTP client, `WebClient`. The
`WebClient` is a very powerful HTTP client and supports methods such as `get(...)`
`post(...)`, `put(...)` ... to perform HTTP requests.

To send the request, `send()` method can be called on the client or to send a
request with JSON body, we can use `sendJsonObject(...)`

```java

import io.vertx.core.json.JsonObject;

@ExtendWith(VertxExtension.class)
public class TestApi extends AbstractRestApiTest {

  private static final Logger LOG = LoggerFactory.getLogger(TestAssetsRestApi.class);

  @Test
  void send_request_without_body(Vertx vertx, VertxTestContext testContext) throws Throwable {
    var client = WebClient.create(vertx, new WebClientOptions().setDefaultPort(TEST_SERVER_PORT));
    client
      .get("/assets")
      .send()
      .onComplete(testContext.succeeding(response -> {
        // ...
        testContext.completeNow();
      }));
  }

  @Test
  void send_request_without_body(Vertx vertx, VertxTestContext testContext) throws Throwable {
    var client = WebClient.create(vertx, new WebClientOptions().setDefaultPort(TEST_SERVER_PORT));
    client
      .post("/assets")
      .sendJsonObject(new JsonObject().put("item_id", 1))
      .onComplete(testContext.succeeding(response -> {
        // ...
        testContext.completeNow();
      }));
  }

}
```

Refer [TestAssetsRestApi.java](src/test/java/com/example/broker/assets/TestAssetsRestApi.java)

## Path variables

A path variable can be defined on an endpoint by prepending the variable name with
a *:*(colon) in the route endpoint. The parameter will be dynamic and can be
accessed via the `routingContext` object in the handler using the
`pathParam(param_name)` method.

For example, we can define a path variable named `asset` as

```java
import io.vertx.core.Vertx;
import io.vertx.ext.web.Router;

public class MainVerticle extends AbstarctVerticle {
  // ...

  @Override
  public void start(Promise<Void> startPromise) throws Exception {
    Router.router(Vertx.vertx())
      .get("/quotes/:assets")
      .handler(routingContext -> {
        System.out.println(routingContext.pathParam("asset"));
      });
  }
}
```

Refer
[QuotesRestApi.java](src/main/java/com/example/broker/quotes/QuotesRestApi.java)
for complete code.

## Request body

The request body is accessible via `routingContext.getBodyAsJson()` which returns
a `import io.vertx.core.json.JsonObject` object.

However, the `BodyHandler` is not enabled in routes by default so the result might
not be as expected and a warning is generated. A `BodyHandler` is registered
explicitly and per route (or can be enabled globally).

The `BodyHandler` can also have custom properties set by calling the respective
methods.

```java
import com.example.broker.MainVerticle;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Vertx;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;

public class mainVerticle extends AbstractVerticle {
  public static void main(String[] args) {
    Vertx vertx = Vertx.vertx();
    vertx.deployVerticle(new MainVerticle());
  }

  @Override
  public void start(Promise<Void> startPromise) {
    final Router router = Router.router(vertx);

    router.route().handler(BodyHandler.create()); // globally

    router.put("/new")
      .handler(BodyHandler
        .create()
        .setBodyLimit(1024)
        .setHandleFileUploads(true))
      .handler(routingContext -> {
        // ...
      }); // Individual route
  }
}
```

See [RestApiVerticle.java](src/main/java/com/example/broker/RestApiVerticle.java)

## Headers

Headers can be added to a HTTP response using
`routingContext.response().putHeader(name, value)`. To avoid typos in for standard
header names, use the constants defined in `HttpHeaders` class and for avoiding
typos in values use the `HttpHeaderValues` class, in the
`io.vertx.core.http.HttpHeaders` and `io.netty.handler.codec.http.HttpHeaderValues`
packages respectively. Since these constants are of type `AsciiString` and the
`putheader(...)` method expects arguments of type `String`, convert them to
`String` values (using `toString()` method) before passing to `putHeader` method.

On client side, the headers can be retrieved as `response.getHeader(name, value)`/

```java
import io.netty.handler.codec.http.HttpHeaderValues;
import io.vertx.core.http.HttpHeaders;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;

public class Router implements Handler<RoutingContext> {
  @Override
  public void handle(RoutingContext routingContext) {
    routingContext
      .response()
      .putHeader(
        HttpHeaders.CONTENT_TYPE.toString(),
        HttpHeaderValues.APPLICATION_JSON
      ).end();
  }
}
```

See
[AssetRestApi.java](src/main/java/com/example/broker/assets/AssetsRestApi.java) for
sending headers and
[testAssetsRestApi.java](src/test/java/com/example/broker/assets/TestAssetsRestApi.java)
for receiving headers examples.

Note that the `HttpHeaders` and `HttpHeadervalues` classes are just used here to
avoid typos in standard header names and values. HTTP headers are just key/value
pairs and can also have custom string values as well.

## Scaling server

- HTTP Server runs on 1 Event Loop Thread
- All requests handled on same thread
- Multiple HTTP Servers can be started on different threads
- Load can be distributed by different servers running on different threads
- 1 Thread will be reserved for the `MainVerticle`

See [MainVerticle](src/main/java/com/example/broker/MainVerticle.java) for an
example deploying verticles based on the number of CPU cores.

# Vert.x Config

Vert.x has an inbuilt powerful Config system. It supports

- JSON
- Properties
- YAML (via extension)
- Hocon (via extension)

formats, and

- system properties
- environment properties
- files
- Event Bus
- Kubernetes Config Map (via extension)
- & many more

stores.

An environment store can be configured as
`var envStore = new ConfigStoreOptions().setType(typeStr).setConfig(jsonObject)`

The `setConfig` method takes in a `JsonObject` as input. Few key values are:

| Key   | Value Type   | Description                                                 |
|-------|--------------|-------------------------------------------------------------|
| keys  | List<String> | List of exposed keys                                        |
| cache | Boolean      | Cache the env or not                                        |
| path  | String       | Path (or name if in resources) of the config json/yaml file |

Then the store can be added to config as
`var retriever = ConfigRetriever.create(vertx, new ConfigRetrieverOptions().addStore(store))`

Vert.x config also allows to add multiple stores, so the values can be overridden.
For example, in

```java
import io.vertx.config.ConfigRetriever;
import io.vertx.config.ConfigRetrieverOptions;
import io.vertx.core.Vertx;

class ConfigLoader {
  public static void load() {
    var retriever = ConfigRetriever.create(Vertx.vertx(),
      new ConfigRetrieverOptions()
        .addStore() // store 1
        .addStore() // store 2
        .addStore() // store 3
    );
  }
}
```

the config values defined by store 1 are overridden by values in store 2 and
similarly config values in store 1 and store 2 are overridden by values in store 3.

The config can then later be retrieved as `retriever.getConfig()` which will return
`Future<JsonObject>` value. This can be mapped to a custom Object to add type safety
to the config values.

The config values can be then used as

```java
import io.vertx.core.AbstractVerticle;

class ExampleVerticle extends AbstractVerticle {

  @Override
  public void start(final Promise<Void> startPromise) throws Exception {
    ConfigLoader.load()
      .onFailure(startPromise::fail)
      .onSuccess(config -> {
        // config contains the config values as JsonObject
      });
  }
}
```
See [ConfigLoader.java](src/main/java/com/example/broker/config/ConfigLoader.java)
for complete example.

In cases like test environment, the system property can also be set manually as
`System.setProperty(key, value)`






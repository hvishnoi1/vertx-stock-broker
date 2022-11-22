package com.example.broker.assets;

import io.netty.handler.codec.http.HttpHeaderValues;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;

public class AssetsRestApi {
  private static final Logger LOG = LoggerFactory.getLogger(AssetsRestApi.class);
  public static final List<String> ASSETS = Arrays.asList("AAPL", "AMZN", "FB", "GOOG", "MSFT", "NFLX", "TSLA");

  public static void attach(Router parent) {
    parent.get("/assets").handler(context -> {
      final JsonArray response = new JsonArray();
      ASSETS.stream().map(Asset::new).forEach(response::add);

      LOG.info("Path {} responds with {}", context.normalizedPath(), response.encode());
      try {
        Thread.sleep(200); // Simulated delay
      } catch (InterruptedException e) {
        throw new RuntimeException(e);
      }
      context.response()
        .putHeader(HttpHeaders.CONTENT_TYPE, HttpHeaderValues.APPLICATION_JSON)
        .putHeader("my-header", "my-value")
        .end(response.toBuffer());
    });
  }
}

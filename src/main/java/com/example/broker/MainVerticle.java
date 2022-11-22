package com.example.broker;

import com.example.broker.assets.AssetsRestApi;
import com.example.broker.config.ConfigLoader;
import com.example.broker.quotes.QuotesRestApi;
import com.example.broker.watchlist.WatchListRestApi;
import io.vertx.core.*;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MainVerticle extends AbstractVerticle {

  //  public static final int PORT = 8888;
  private static final Logger LOG = LoggerFactory.getLogger(MainVerticle.class);

  public static void main(String[] args) {
//    System.setProperty(ConfigLoader.SERVER_PORT, "9000");
    var vertx = Vertx.vertx();
    vertx.exceptionHandler(error -> LOG.error("Unhandled: {}", error));
//    vertx.deployVerticle(new MainVerticle(), ar -> {
//      if (ar.failed()) {
//        LOG.error("Failed to deploy: {}", ar.cause());
//        return;
//      }
//      LOG.info("Deployed {}!", MainVerticle.class.getName());
//    });
    vertx.deployVerticle(new MainVerticle())
      .onFailure(err -> {
        LOG.error("Failed to deploy: {}", err);
      }).onSuccess(id -> {
        LOG.info("Deployed {} with id {}", MainVerticle.class.getSimpleName(), id);
      });
  }

  @Override
  public void start(Promise<Void> startPromise) throws Exception {
    vertx.deployVerticle(
        VersionInfoVerticle.class.getName())
      .onFailure(startPromise::fail)
      .onSuccess(id -> {
        LOG.info("Deployed {} with id {}", VersionInfoVerticle.class.getSimpleName(), id);
      })
      .compose(next ->
        deployeRestApiVerticle(startPromise)
      );
  }

  private Future<String> deployeRestApiVerticle(Promise<Void> startPromise) {
    return vertx.deployVerticle(
        RestApiVerticle.class.getName(),
        new DeploymentOptions()
          .setInstances(processors()))
      .onFailure(startPromise::fail)
      .onSuccess(id -> {
        LOG.info("Deployed {} with id {}", RestApiVerticle.class.getSimpleName(), id);
        startPromise.complete();
      });
  }

  private static int processors() {
    return Math.max(1, Runtime.getRuntime().availableProcessors());
  }

}

package com.example.broker.watchlist;

import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Optional;
import java.util.UUID;

public class GetWatchListHandler implements Handler<RoutingContext> {

  private static final Logger LOG = LoggerFactory.getLogger(GetWatchListHandler.class);
  final HashMap<UUID, WatchList> watchListPerAccount;

  public GetWatchListHandler(HashMap<UUID, WatchList> watchListPerAccount) {
    this.watchListPerAccount = watchListPerAccount;
  }

  @Override
  public void handle(RoutingContext routingContext) {
    String accountId = WatchListRestApi.getAccountId(routingContext);
    var watchList = Optional.ofNullable(watchListPerAccount.get(UUID.fromString(accountId)));
    if (watchList.isEmpty()) {
      routingContext.response()
        .setStatusCode(HttpResponseStatus.NOT_FOUND.code())
        .end(new JsonObject()
          .put("message", "watchlist for account " + accountId + " not available")
          .put("path", routingContext.normalizedPath())
          .toBuffer()
        );
      return;
    }
    routingContext.response().end(watchList.get().toJsonObject().toBuffer());

  }
}

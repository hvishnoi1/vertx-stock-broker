package com.example.broker.watchlist;

import io.vertx.core.Handler;
import io.vertx.ext.web.RoutingContext;

import java.util.HashMap;
import java.util.UUID;

public class PutWatchListHandler implements Handler<RoutingContext> {
  final HashMap<UUID, WatchList> watchListPerAccount;
  public PutWatchListHandler(HashMap<UUID, WatchList> watchListPerAccount) {
    this.watchListPerAccount = watchListPerAccount;
  }

  @Override
  public void handle(RoutingContext routingContext) {
    String accountId = WatchListRestApi.getAccountId(routingContext);

    var json = routingContext.getBodyAsJson();
    var watchList = json.mapTo(WatchList.class);
    watchListPerAccount.put(UUID.fromString(accountId), watchList);
    routingContext.response().end(json.toBuffer());
  }
}

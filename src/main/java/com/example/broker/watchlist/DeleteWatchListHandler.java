package com.example.broker.watchlist;

import io.vertx.core.Handler;
import io.vertx.ext.web.RoutingContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.UUID;

public class DeleteWatchListHandler implements Handler<RoutingContext> {

  private static final Logger LOG = LoggerFactory.getLogger(DeleteWatchListHandler.class);
  final HashMap<UUID, WatchList> watchListPerAccount;

  public DeleteWatchListHandler(HashMap<UUID, WatchList> watchListPerAccount) {
    this.watchListPerAccount = watchListPerAccount;
  }

  @Override
  public void handle(RoutingContext routingContext) {
    String accountId = WatchListRestApi.getAccountId(routingContext);
    final WatchList deleted = watchListPerAccount.remove(UUID.fromString(accountId));
    LOG.info("Deleted: {} Remaining: {}", deleted, watchListPerAccount.values());
    routingContext.response()
      .end(deleted.toJsonObject().toBuffer());
  }
}

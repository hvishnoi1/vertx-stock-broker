package com.example.broker.quotes;

import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Optional;

public class GetQuoteHandler implements Handler<RoutingContext> {

  private static final Logger LOG = LoggerFactory.getLogger(GetQuoteHandler.class);

  public GetQuoteHandler(Map<String, Quote> cachedQuotes) {
    this.cachedQuotes = cachedQuotes;
  }

  private final Map<String,Quote> cachedQuotes;



  @Override
  public void handle(RoutingContext routingContext) {
    final String assetparam = routingContext.pathParam("asset");
    LOG.debug("Asset parameter: {}", assetparam);

    Optional<Quote> maybeQuote = Optional.ofNullable(cachedQuotes.get(assetparam));
    if (maybeQuote.isEmpty()) {
      routingContext
        .response()
        .setStatusCode(HttpResponseStatus.NOT_FOUND.code())
        .end(new JsonObject()
          .put("message", "quote for asset " + assetparam + " not available!")
          .put("path", routingContext.normalizedPath())
          .toBuffer()
        );
      return;
    }

    final JsonObject response = maybeQuote.get().toJsonObject();
    LOG.info("Path {} responds with {}", routingContext.normalizedPath(), response.encode());
    routingContext.response().end(response.toBuffer());
  }
}

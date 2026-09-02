package com.locus.gateway.filter;

import java.util.UUID;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

// Generates a fresh correlation ID for every request, never trusting a client-supplied one, per
// technical-spec.md §2/§7 — a malicious or buggy client can't inject a value that collides with
// or spoofs another request's trace.
@Component
public class CorrelationIdFilter implements GlobalFilter, Ordered {

  public static final String HEADER = "X-Correlation-Id";
  public static final String ATTRIBUTE = "correlationId";

  @Override
  public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
    String correlationId = UUID.randomUUID().toString();
    ServerHttpRequest request = exchange.getRequest().mutate().header(HEADER, correlationId).build();
    exchange.getAttributes().put(ATTRIBUTE, correlationId);
    return chain.filter(exchange.mutate().request(request).build());
  }

  @Override
  public int getOrder() {
    return Ordered.HIGHEST_PRECEDENCE;
  }
}

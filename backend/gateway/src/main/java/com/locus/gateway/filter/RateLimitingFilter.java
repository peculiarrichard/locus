package com.locus.gateway.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.locus.gateway.web.ErrorEnvelope;
import java.net.InetSocketAddress;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

// Two-tier rate limiting per frd.md's API Gateway section — per-IP on public auth routes,
// per-authenticated-user everywhere else — implemented directly against Redis rather than Spring
// Cloud Gateway's built-in RedisRateLimiter, whose behavior on a Redis error isn't documented.
// Fails CLOSED here explicitly: a rate limiter failing open defeats its purpose during exactly
// the kind of incident it exists for.
@Component
public class RateLimitingFilter implements GlobalFilter, Ordered {

  private static final List<String> PUBLIC_EXACT_PATHS = List.of("/api/v1/auth/register", "/api/v1/auth/login",
      "/api/v1/auth/mfa/challenge", "/api/v1/auth/refresh", "/api/v1/auth/verify-email",
      "/api/v1/auth/verify-email/resend");
  private static final String PUBLIC_PREFIX = "/api/v1/auth/password-reset/";

  private final ReactiveStringRedisTemplate redisTemplate;
  private final ObjectMapper objectMapper;

  @Value("${locus.gateway.rate-limit.per-ip.limit}")
  private int perIpLimit;

  @Value("${locus.gateway.rate-limit.per-ip.window-seconds}")
  private long perIpWindowSeconds;

  @Value("${locus.gateway.rate-limit.per-user.limit}")
  private int perUserLimit;

  @Value("${locus.gateway.rate-limit.per-user.window-seconds}")
  private long perUserWindowSeconds;

  public RateLimitingFilter(ReactiveStringRedisTemplate redisTemplate, ObjectMapper objectMapper) {
    this.redisTemplate = redisTemplate;
    this.objectMapper = objectMapper;
  }

  @Override
  public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
    String path = exchange.getRequest().getURI().getPath();
    if (isPublicAuthPath(path)) {
      return limit(exchange, chain, ipKey(exchange), perIpLimit, perIpWindowSeconds);
    }
    return ReactiveSecurityContextHolder.getContext().map(ctx -> ctx.getAuthentication().getName())
        .defaultIfEmpty("anonymous").flatMap(userKey -> limitForUser(exchange, chain, userKey));
  }

  private Mono<Void> limitForUser(ServerWebExchange exchange, GatewayFilterChain chain, String userKey) {
    return limit(exchange, chain, "user:" + userKey, perUserLimit, perUserWindowSeconds);
  }

  private boolean isPublicAuthPath(String path) {
    return PUBLIC_EXACT_PATHS.contains(path) || path.startsWith(PUBLIC_PREFIX);
  }

  private String ipKey(ServerWebExchange exchange) {
    InetSocketAddress remote = exchange.getRequest().getRemoteAddress();
    return "ip:" + (remote != null ? remote.getAddress().getHostAddress() : "unknown");
  }

  private Mono<Void> limit(ServerWebExchange exchange, GatewayFilterChain chain, String key, int limit,
      long windowSeconds) {
    String redisKey = "rate-limit:" + key;
    return redisTemplate.opsForValue().increment(redisKey).flatMap(count -> {
      Mono<Boolean> expireStep = count == 1
          ? redisTemplate.expire(redisKey, Duration.ofSeconds(windowSeconds))
          : Mono.just(true);
      return expireStep.then(count <= limit ? chain.filter(exchange) : tooManyRequests(exchange));
    }).onErrorResume(e -> tooManyRequests(exchange));
  }

  private Mono<Void> tooManyRequests(ServerWebExchange exchange) {
    ServerHttpResponse response = exchange.getResponse();
    response.setStatusCode(HttpStatus.TOO_MANY_REQUESTS);
    response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
    ErrorEnvelope envelope = new ErrorEnvelope("RATE_LIMITED", "Too many requests", correlationId(exchange),
        Instant.now());
    byte[] bytes;
    try {
      bytes = objectMapper.writeValueAsBytes(envelope);
    } catch (Exception e) {
      bytes = "{}".getBytes();
    }
    return response.writeWith(Mono.just(response.bufferFactory().wrap(bytes)));
  }

  private String correlationId(ServerWebExchange exchange) {
    Object id = exchange.getAttribute(CorrelationIdFilter.ATTRIBUTE);
    return id != null ? id.toString() : UUID.randomUUID().toString();
  }

  @Override
  public int getOrder() {
    return Ordered.HIGHEST_PRECEDENCE + 1;
  }
}

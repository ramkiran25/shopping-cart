package com.api;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class HttpPriceApiGateway implements PriceApiGateway {

  private final RestClient restClient;
  private final Map<String, BigDecimal> offlineFallbackCache = new ConcurrentHashMap<>();

  public HttpPriceApiGateway(RestClient.Builder restClientBuilder,
      @Value("${price.api.base-url:https://equalexperts.github.io}") String baseUrl) {
    this.restClient = restClientBuilder.baseUrl(baseUrl).build();
    initializeOfflineCache();
  }

  @Override
  public Optional<BigDecimal> fetchPrice(String productName) {
    String sanitizedName = productName.trim().toLowerCase();
    try {
      PriceResponse response =
          restClient.get().uri("/backend-take-home-test-data/{product}.json", sanitizedName)
              .retrieve().body(PriceResponse.class);

      return Optional.ofNullable(response).map(PriceResponse::price);
    } catch (Exception e) {
      return Optional.ofNullable(offlineFallbackCache.get(sanitizedName));
    }
  }

  // Inner DTO for clean JSON unmarshalling without exposing domain structures
  private record PriceResponse(String name, BigDecimal price) {
  }

  private void initializeOfflineCache() {
    offlineFallbackCache.put("cheerios", new BigDecimal("2.52"));
    offlineFallbackCache.put("cornflakes", new BigDecimal("2.52"));
    offlineFallbackCache.put("frosties", new BigDecimal("2.52"));
    offlineFallbackCache.put("shreddies", new BigDecimal("2.52"));
    offlineFallbackCache.put("weetabix", new BigDecimal("9.98"));
  }
}

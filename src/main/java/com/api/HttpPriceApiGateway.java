package com.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.exception.ProductNotFoundException;
import com.exception.UpstreamDependencyException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Optional;

@Component
public class HttpPriceApiGateway implements PriceApiGateway {

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final String baseUrl;

    public HttpPriceApiGateway(
            HttpClient httpClient, 
            ObjectMapper objectMapper, 
            @Value("${price.api.base-url:https://equalexperts.github.io}") String baseUrl) {
        this.httpClient = httpClient;
        this.objectMapper = objectMapper;
        this.baseUrl = baseUrl;
    }

    @Override
    public Optional<BigDecimal> fetchPrice(String productName) {
        String sanitizedName = productName.trim().toLowerCase();
        String targetUrl = String.format("%s/backend-take-home-test-data/%s.json", baseUrl, sanitizedName);

        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(targetUrl))
                    .timeout(Duration.ofSeconds(5))
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                JsonNode root = objectMapper.readTree(response.body());
                if (root.has("price")) {
                    return Optional.of(root.get("price").decimalValue());
                }
                return Optional.empty();
            } else if (response.statusCode() == 404) {
                throw new ProductNotFoundException("Product not found in upstream catalog: " + sanitizedName);
            } else {
                throw new UpstreamDependencyException("Upstream pricing API returned an error status: " + response.statusCode());
            }
        } catch (ProductNotFoundException | UpstreamDependencyException e) {
            throw e;
        } catch (Exception ex) {
            throw new UpstreamDependencyException("Failed to communicate with upstream pricing system", ex);
        }
    }
}
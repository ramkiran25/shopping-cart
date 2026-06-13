package com;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.service.ShoppingCart;
import com.service.FlatRateTaxStrategy;
import com.api.PriceApiGateway;
import com.model.CartState;

public class ShoppingCartConcurrencyTest {

  // Aligned mock gateway to cleanly implement the exact Optional production contract
  private static class MockPriceGateway implements PriceApiGateway {
    @Override
    public Optional<BigDecimal> fetchPrice(String productName) {
      return Optional.of(new BigDecimal("10.00")); // Every item costs 10.00 base price
    }
  }

  @DisplayName("High-Scale Concurrency Validation — Race Condition Protection on Single Cart")
  @Test
  public void testCartAggregationUnderConcurrentBombardment() throws InterruptedException {
    int requestCount = 100;
    int itemsPerRequest = 2;

    // Setup shared single cart instance with a 12.5% tax strategy
    PriceApiGateway mockGateway = new MockPriceGateway();
    ShoppingCart sharedCart =
        new ShoppingCart(mockGateway, new FlatRateTaxStrategy(new BigDecimal("0.125")));

    ExecutorService executor = Executors.newFixedThreadPool(16);
    CountDownLatch readyLatch = new CountDownLatch(requestCount);
    CountDownLatch startLatch = new CountDownLatch(1);
    CountDownLatch finishLatch = new CountDownLatch(requestCount);

    AtomicInteger exceptionCount = new AtomicInteger(0);

    for (int i = 0; i < requestCount; i++) {
      executor.submit(() -> {
        readyLatch.countDown();
        try {
          // Block until the master gate opens to force simultaneous execution threads
          startLatch.await();

          // Simulating an incoming asynchronous API thread adding items to the same cart
          sharedCart.addProduct("cheerios", itemsPerRequest);

        } catch (Exception e) {
          exceptionCount.incrementAndGet();
        } finally {
          finishLatch.countDown();
        }
      });
    }

    // Wait for all worker threads to be spun up and ready
    readyLatch.await(5, TimeUnit.SECONDS);

    // Open the floodgates!
    startLatch.countDown();

    // Wait up to 10 seconds for all operations to settle
    boolean cleanExit = finishLatch.await(10, TimeUnit.SECONDS);
    executor.shutdownNow();

    // Assertions
    assertTrue(cleanExit, "The cart state modifications deadlocked or timed out.");
    assertEquals(0, exceptionCount.get(),
        "Exceptions were thrown during multi-threaded mutations!");

    // Calculations verification:
    // Total items expected = 100 requests * 2 items = 200 items.
    // Base subtotal = 200 items * 10.00 = 2000.00
    // Expected total quantities must be exactly right if no updates were dropped!
    CartState finalizedState = sharedCart.getState();

    System.out.println("Finalized Subtotal calculated: " + finalizedState.subtotal());

    // Validation Check: If addProduct() isn't synchronized, this assertion will catch the race
    // condition drop.
    assertEquals(new BigDecimal("2000.00"), finalizedState.subtotal(),
        "Race condition detected! The cart dropped item quantity updates under heavy concurrent load.");
  }
}

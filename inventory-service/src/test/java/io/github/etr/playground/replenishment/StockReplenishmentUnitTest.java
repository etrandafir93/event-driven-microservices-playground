package io.github.etr.playground.replenishment;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import io.github.etr.playground.reservation.StockReservationOutcome;

class StockReplenishmentUnitTest {

    private RetailSupplierClient retailSupplier = mock();

    private ReplenishmentThreshold threshold = new ReplenishmentThreshold().defaultValue(100);

    private StockReplenishment stockReplenishment = new StockReplenishment(threshold, retailSupplier);

    @Test
    void shouldRequestReplenishment_whenStockIsBelowThreshold() {
        var event = new StockReservationOutcome.Success("order-123", "john_doe", "SKU-123", 5, 50);

        stockReplenishment.accept(event);

        verify(retailSupplier).sendStockReplenishmentRequest("SKU-123", 200);
    }

    @Test
    void shouldNotRequestReplenishment_whenStockIsAboveThreshold() {
        var event = new StockReservationOutcome.Success("order-123", "john_doe", "SKU-123", 5, 150);

        stockReplenishment.accept(event);

        verify(retailSupplier, never()).sendStockReplenishmentRequest("SKU-123", 200);
    }

    @Test
    void shouldNotRequestReplenishment_whenStockIsExactlyAtThreshold() {
        var event = new StockReservationOutcome.Success("order-123", "john_doe", "SKU-123", 5, 100);

        stockReplenishment.accept(event);

        verify(retailSupplier, never()).sendStockReplenishmentRequest("SKU-123", 200);
    }

    @ParameterizedTest
    @CsvSource({
        "50, 100, 200",
        "25, 50, 100",
        "9, 10, 20"
    })
    void shouldRequestDoubleTheThreshold_whenReplenishing(int stockAvailable, int thresholdValue, int expectedQuantity) {
        threshold.defaultValue(thresholdValue);

        var event = new StockReservationOutcome.Success("order-123", "john_doe", "SKU-123", 5, stockAvailable);

        stockReplenishment.accept(event);

        verify(retailSupplier).sendStockReplenishmentRequest("SKU-123", expectedQuantity);
    }

    @Test
    void shouldRequestReplenishment_whenStockIsJustBelowThreshold() {
        var event = new StockReservationOutcome.Success("order-123", "john_doe", "SKU-123", 1, 99);

        stockReplenishment.accept(event);

        verify(retailSupplier).sendStockReplenishmentRequest("SKU-123", 200);
    }
}
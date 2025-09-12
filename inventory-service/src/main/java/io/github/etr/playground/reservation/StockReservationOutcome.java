package io.github.etr.playground.reservation;

public sealed interface StockReservationOutcome
    permits StockReservationOutcome.Success, StockReservationOutcome.Failure {

    String orderId();
    String itemSku();
    int stockRequested();

    record Success(String orderId, String itemSku, int stockRequested, int stockAvailable) implements StockReservationOutcome {
    }

    sealed interface Failure extends StockReservationOutcome permits OutOfStock, UnknownItem {
    }

    record OutOfStock(String orderId, String itemSku, int stockRequested) implements Failure {
    }

    record UnknownItem(String orderId, String itemSku, int stockRequested) implements Failure {
    }
}

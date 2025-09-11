package io.github.etr.playground.reservation;

import com.sun.net.httpserver.Authenticator;

public sealed interface Outcome
    permits Outcome.Success, Outcome.Failure {

    String orderId();
    String itemSku();
    int quantity();

    record Success(String orderId, String itemSku, int quantity) implements Outcome {
    }

    sealed interface Failure extends Outcome permits OutOfStock, UnknownItem {
    }

    record OutOfStock(String orderId, String itemSku, int quantity) implements Failure {
    }

    record UnknownItem(String orderId, String itemSku, int quantity) implements Failure {
    }
}

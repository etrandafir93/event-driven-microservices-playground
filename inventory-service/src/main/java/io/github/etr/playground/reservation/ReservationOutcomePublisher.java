package io.github.etr.playground.reservation;

import static org.springframework.messaging.support.MessageBuilder.withPayload;

import java.util.function.Function;

import jakarta.annotation.Nullable;

import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;

import io.github.etr.playground.application.Filter;
import io.github.etr.playground.reservation.StockReservationOutcome.Failure;
import io.github.etr.playground.reservation.StockReservationOutcome.Success;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Filter("reservationOutcomePublisher")
@RequiredArgsConstructor
class ReservationOutcomePublisher implements Function<StockReservationOutcome, Message<ReservationOutcomePublisher.ReservationCompletedEvent>> {

    private final ReservationOutcomeChannels downstream;

    @Override
    public Message<ReservationCompletedEvent> apply(StockReservationOutcome outcome) {
        var reservationCompletedEvent = switch (outcome) {
            case Success ok -> new ReservationCompletedEvent(ok.itemSku(), ok.orderId(), ok.stockRequested(), ok.stockAvailable());
            case Failure nok -> new ReservationCompletedEvent(nok.itemSku(), nok.orderId(), nok.stockRequested(), null);
        };

        var msg = withPayload(reservationCompletedEvent);
        msg = withTopic(msg, outcome);
        msg = withKey(msg, outcome.itemSku());
        return msg.build();
    }

    private MessageBuilder<ReservationCompletedEvent> withTopic(MessageBuilder<ReservationCompletedEvent> msg, StockReservationOutcome outcome) {
        String header = switch (outcome) {
            case Success __ -> downstream.successChannel();
            case Failure __ -> downstream.failureChannel();
        };
        return msg.setHeader("spring.cloud.stream.sendto.destination", header);
    }

    private MessageBuilder<ReservationCompletedEvent> withKey(MessageBuilder<ReservationCompletedEvent> msg, String key) {
        return msg.setHeader(KafkaHeaders.KEY, key.getBytes());
    }

    record ReservationCompletedEvent(String itemSku, String orderId, int stockRequested, @Nullable Integer stockAvailable) {
    }

    record ReservationOutcomeChannels(String successChannel, String failureChannel) {
    }

}
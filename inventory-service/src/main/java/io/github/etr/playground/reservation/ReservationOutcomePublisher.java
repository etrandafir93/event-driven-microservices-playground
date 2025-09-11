package io.github.etr.playground.reservation;

import static org.springframework.messaging.support.MessageBuilder.withPayload;

import java.util.function.Function;

import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;

import io.github.etr.playground.application.Filter;
import io.github.etr.playground.reservation.Outcome.Failure;
import io.github.etr.playground.reservation.Outcome.Success;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Filter("reservationOutcomePublisher")
@RequiredArgsConstructor
class ReservationOutcomePublisher implements Function<Outcome, Message<ReservationOutcomePublisher.ReservationCompletedEvent>> {

    private final ReservationOutcomeChannels downstream;

    @Override
    public Message<ReservationCompletedEvent> apply(Outcome outcome) {
        var reservationCompletedEvent = new ReservationCompletedEvent(
            outcome.itemSku(), outcome.orderId(), outcome.quantity());

        var msg = withPayload(reservationCompletedEvent);
        msg = withTopic(msg, outcome);
        msg = withKey(msg, outcome.itemSku());
        return msg.build();
    }

    private MessageBuilder<ReservationCompletedEvent> withTopic(MessageBuilder<ReservationCompletedEvent> msg, Outcome outcome) {
        String header = switch (outcome) {
            case Success __ -> downstream.successChannel();
            case Failure __ -> downstream.failureChannel();
        };
        return msg.setHeader("spring.cloud.stream.sendto.destination", header);
    }

    private MessageBuilder<ReservationCompletedEvent> withKey(MessageBuilder<ReservationCompletedEvent> msg, String key) {
        return msg.setHeader(KafkaHeaders.KEY, key.getBytes());
    }

    record ReservationCompletedEvent(String itemSku, String orderId, int quantity) {}

    record ReservationOutcomeChannels(String successChannel, String failureChannel) {
    }

}
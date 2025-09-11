package io.github.etr.playground.reservation;

import static org.springframework.messaging.support.MessageBuilder.withPayload;

import java.util.function.Function;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;

import io.github.etr.playground.reservation.Outcome.Failure;
import io.github.etr.playground.reservation.Outcome.Success;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component("reservationOutcomePublisher")
@RequiredArgsConstructor
class ReservationOutcomePublisher implements Function<Outcome, Message<ReservationOutcomePublisher.ReservationCompletedEvent>> {

    @Value("${spring.cloud.stream.bindings.itemReservationAttempt|reservationOutcomePublisher-out-0.success-destination}")
    private String successDestination;

    @Value("${spring.cloud.stream.bindings.itemReservationAttempt|reservationOutcomePublisher-out-0.failure-destination}")
    private String failureDestination;

    @Override
    public Message<ReservationCompletedEvent> apply(Outcome outcome) {
        var reservationCompletedEvent = new ReservationCompletedEvent(
            outcome.itemSku(), outcome.orderId(), outcome.quantity());

        var msg = withPayload(reservationCompletedEvent);
        msg = withDestination(msg, outcome);
        msg = withKey(msg, outcome.itemSku());
        return msg.build();
    }

    record ReservationCompletedEvent(String itemSku, String orderId, int quantity) {}

    private MessageBuilder<ReservationCompletedEvent> withDestination(MessageBuilder<ReservationCompletedEvent> msg, Outcome outcome) {
        String header = switch (outcome) {
            case Success __ -> successDestination;
            case Failure __ -> failureDestination;
        };
        return msg.setHeader("spring.cloud.stream.sendto.destination", header);
    }

    private MessageBuilder<ReservationCompletedEvent> withKey(MessageBuilder<ReservationCompletedEvent> msg, String key) {
        return msg.setHeader(KafkaHeaders.KEY, key.getBytes());
    }
}
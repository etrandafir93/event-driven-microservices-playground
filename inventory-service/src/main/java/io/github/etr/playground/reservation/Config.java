package io.github.etr.playground.reservation;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.github.etr.playground.reservation.ReservationOutcomePublisher.ReservationOutcomeChannels;

@Configuration("reservationConfig")
class Config {

    @Bean
    ReservationOutcomeChannels reservationOutcomeDestination(
        @Value("${spring.cloud.stream.bindings.itemReservationAttempt|reservationOutcomePublisher-out-0.success-channel}") String successChannel,
        @Value("${spring.cloud.stream.bindings.itemReservationAttempt|reservationOutcomePublisher-out-0.failure-channel}") String failureChannel
    ) {
        return new ReservationOutcomeChannels(successChannel, failureChannel);
    }

}

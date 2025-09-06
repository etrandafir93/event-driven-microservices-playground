package io.github.etr.playground.infra;

import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.kafka.core.KafkaOperations;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;

import lombok.RequiredArgsConstructor;
import lombok.experimental.Delegate;

@RequiredArgsConstructor
public class StringKafkaTemplateSpy implements KafkaOperations<String, String> {

    @Delegate(excludes = CanStub.class)
    private final KafkaOperations<String, String> delegate;

    private Function<ProducerRecord<String, String>, ProducerRecord<String, String>> beforeSend = Function.identity();

    @Override
    public CompletableFuture<SendResult<String, String>> send(ProducerRecord<String, String> msg) {
        try {
            msg = beforeSend.apply(msg);
        } catch (Exception e) {
            return CompletableFuture.failedFuture(e);
        }
        return delegate.send(msg);
    }

    public void reset() {
        beforeSend = Function.identity();
    }

    public void beforeSend(Function<ProducerRecord<String, String>, ProducerRecord<String, String>> beforeSend) {
        this.beforeSend = beforeSend;
    }

    private interface CanStub {
        CompletableFuture<SendResult<String, String>> send(ProducerRecord<String, String> record);
    }

}

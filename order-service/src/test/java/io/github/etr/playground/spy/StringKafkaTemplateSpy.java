package io.github.etr.playground.spy;

import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.kafka.core.KafkaOperations;
import org.springframework.kafka.support.SendResult;

import lombok.RequiredArgsConstructor;
import lombok.experimental.Delegate;

@RequiredArgsConstructor
public class StringKafkaTemplateSpy implements KafkaOperations<String, String> {

    private Function<ProducerRecord<String, String>, ProducerRecord<String, String>> beforeSend = Function.identity();

    @Delegate(excludes = CanStub.class) // <- never used this before :)
    private final KafkaOperations<String, String> delegate;

    @Override
    public CompletableFuture<SendResult<String, String>> send(ProducerRecord<String, String> msg) {
        try {
            msg = beforeSend.apply(msg);
        } catch (Exception e) {
            return CompletableFuture.failedFuture(e);
        }
        return delegate.send(msg);
    }

    public void callRealFunction() {
        beforeSend = Function.identity();
    }

    public void send(Function<ProducerRecord<String, String>, ProducerRecord<String, String>> beforeSend) {
        this.beforeSend = beforeSend;
    }

    private interface CanStub {
        CompletableFuture<SendResult<String, String>> send(ProducerRecord<String, String> record);
    }

}

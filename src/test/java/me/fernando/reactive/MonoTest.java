package me.fernando.reactive;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.function.Consumer;
import java.util.function.Function;

public class MonoTest {

    private static final Logger log = LoggerFactory.getLogger(MonoTest.class);

    @Test
    public void monoSubscriber() {
        final String message = "Mono Test =D";

        // Observing all reactive streams, then logging
        Mono<String> mono = Mono.just(message).log();

        // Normal flux: onSubscribe -> request -> onNext -> onComplete
        mono.subscribe();
        log.info("\n");

        // Script for async sequence (step by step)
        StepVerifier.create(mono)
                .expectNext(message) // changing to another string will fail in the test
                .verifyComplete();
    }

    @Test
    public void monoSubscriberConsumer() {
        final String message = "Mono Test =D";

        Mono<String> mono = Mono.just(message).map(new Function<String, String>() {
            @Override
            public String apply(String s) {
                throw new RuntimeException("Mono error thrown!");
            }
        });

        // Error flux: onSubscribe -> request -> onError
        mono.subscribe(new Consumer<String>() {
            @Override
            public void accept(String s) {
                log.info("Message is {}", s);
            }
        }, new Consumer<Throwable>() {
            @Override
            public void accept(Throwable throwable) {
                log.error("Subscriber error thrown!");
                throwable.printStackTrace();
            }
        });

        log.info("\n");

        // Script for async sequence (step by step)
        StepVerifier.create(mono)
                .expectError(RuntimeException.class)
                .verify();

    }
}

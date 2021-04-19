package me.fernando.reactive.publisher;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

public class FluxTest {

    private static final Logger log = LoggerFactory.getLogger(FluxTest.class);

    @Test
    public void fluxSubscriber() {
        final String[] messages = {
                "Message 1", "Message 2", "Message 3", "Message 4"
        };

        // Observing all reactive streams, then logging
        Flux<String> flux = Flux.just(messages).log();

        // Normal flux: onSubscribe -> request
        // -> onNext(1) -> onNext(2) -> onNext(3) -> onNext(4)
        // -> onComplete
        flux.subscribe();
        log.info("\n");

        // Test Script
        StepVerifier.create(flux)
                .expectNext(messages) //on changing some of the messages, will fail in the test
                .verifyComplete();
    }

    @Test
    public void fluxSubscriberNumbers() {
        // Observing all reactive streams, then logging
        Flux<Integer> flux = Flux.range(1, 5).log();

        // Normal flux: onSubscribe -> request
        // -> onNext(1) -> onNext(2) -> onNext(3) -> onNext(4)
        // -> onComplete
        flux.subscribe(n -> log.info("Number {}", n));
        log.info("\n");

        // Test Script
        StepVerifier.create(flux)
                .expectNext(1, 2, 3, 4, 5) //on changing some of the numbers, will fail in the test
                .verifyComplete();
    }

}

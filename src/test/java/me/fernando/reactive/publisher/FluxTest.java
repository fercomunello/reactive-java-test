package me.fernando.reactive.publisher;

import org.junit.jupiter.api.Test;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.BaseSubscriber;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.util.List;

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

    @Test
    public void fluxSubscriberNumbersFromList() {
        // Observing all reactive streams, then logging
        Flux<Integer> flux = Flux.fromIterable(List.of(1, 2, 3, 4, 5)).log();

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

    @Test
    public void fluxSubscriberNumbersErrorListener() {
        // Observing all reactive streams, then logging
        Flux<Integer> flux = Flux.range(1, 5).log()
                .map(n -> {
                    if (n == 3)
                        throw new IndexOutOfBoundsException("Index out of bounds");
                    return n;
                });

        // Normal flux: onSubscribe -> request
        // -> onNext(1) -> onNext(2) -> onNext(3) -> onNext(4) -> cancel
        flux.subscribeWith(new Subscriber<Integer>() {
            @Override
            public void onSubscribe(Subscription subscription) {
                subscription.request(2);
                //subscription.request(3); Throws index out of bounds...
            }

            @Override
            public void onNext(Integer n) {
                log.info("Number {}", n);
            }

            @Override
            public void onError(Throwable e) {
                e.printStackTrace();
            }

            @Override
            public void onComplete() {
                log.info("Completed!!");
            }
        });

        log.info("\n");

        // Test Script
        StepVerifier.create(flux)
                .expectNext(1, 2)
                .expectError(IndexOutOfBoundsException.class)
                .verify();
    }

    @Test
    public void fluxSubscriberNumbersWithBackpressure() {
        // Observing all reactive streams, then logging
        Flux<Integer> flux = Flux.range(1, 10)
                .log();

        // Backpressure:
        // Subscribe a flux for each pair integers (1 ~ 10 range)
        flux.subscribe(new BaseSubscriber<>() {
            private int count = 0;
            private final int requestCount = 2;

            @Override
            protected void hookOnSubscribe(Subscription subscription) {
                request(requestCount);
            }

            @Override
            protected void hookOnNext(Integer value) {
                //  do business logic ...

                // do backpressure logic
                count++;
                if (count >= requestCount) {
                    count = 0;
                    request(requestCount);
                }
            }
        });

        log.info("\n");

        // Test Script
        StepVerifier.create(flux)
                .expectNext(1, 2, 3, 4, 5, 6, 7, 8, 9, 10)
                .verifyComplete();
    }

}

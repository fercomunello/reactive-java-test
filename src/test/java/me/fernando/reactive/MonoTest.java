package me.fernando.reactive;

import org.junit.jupiter.api.Test;
import org.reactivestreams.Subscription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

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

        // Test Script
        StepVerifier.create(mono)
                .expectNext(message) // changing to another string will fail in the test
                .verifyComplete();
    }

    @Test
    public void monoSubscriberConsumer() {
        final String message = "Mono Test =D";

        Mono<String> mono = Mono.just(message).map(s -> {
            throw new RuntimeException("Mono error thrown!");
        });

        // Error flux: onSubscribe -> request -> onError
        mono.subscribe(s -> log.info("Message is {}", s), throwable -> {
            log.error("Subscriber error thrown!");
            throwable.printStackTrace();
        });

        log.info("\n");

        // Test Script
        StepVerifier.create(mono)
                .expectError(RuntimeException.class)
                .verify();
    }

    @Test
    public void monoSubscriberConsumerComplete() {
        final String message = "Mono Test =D";

        // Collect and log upper-case message stream
        Mono<String> mono = Mono.just(message).log()
                .map(String::toUpperCase);

        // Flux: onSubscribe -> request (unbounded) -> onNext -> onComplete
        mono.subscribe(s -> log.info("Message is {}", s),
                Throwable::printStackTrace, () -> log.info("Completed!!"));

        log.info("\n");

        // Test Script
        StepVerifier.create(mono)
                .expectNext(message.toUpperCase())
                .verifyComplete();
    }

    @Test
    public void monoSubscriberConsumerSubscriptionCancel() {
        final String message = "Mono Test =D";

        // Collect and log upper-case message stream
        Mono<String> mono = Mono.just(message).log()
                .map(String::toUpperCase);

        // Flux: onSubscribe -> cancel
        mono.subscribe(s -> log.info("Message is {}", s),
                Throwable::printStackTrace, () -> log.info("Completed!!"),
                Subscription::cancel);

        log.info("\n");

        // Test Script
        StepVerifier.create(mono)
                .expectNext(message.toUpperCase())
                .verifyComplete();
    }

    @Test
    public void monoSubscriberConsumerSubscriptionBoundedCancel() {
        final String message = "Mono Test =D";

        // Collect and log upper-case message stream
        Mono<String> mono = Mono.just(message).log()
                .map(String::toUpperCase);

        // Flux: onSubscribe ->
        mono.subscribe(s -> log.info("Message is {}", s),
                Throwable::printStackTrace, () -> log.info("Completed!!"),
                subscription -> subscription.request(5L));

        log.info("\n");

        // Test Script
        StepVerifier.create(mono)
                .expectNext(message.toUpperCase())
                .verifyComplete();
    }

    @Test
    public void monoSubscriberListeners() {
        final String message = "Mono Test =D";

        // Collect and log upper-case message stream
        // Set the consumers for Mono listenres
        Mono<Object> mono = Mono.just(message).log()
                .map(String::toUpperCase)
                .doOnSubscribe(subscription -> log.info("Subscribed!!"))
                .doOnRequest(value -> log.info("Request Received ..."))
                // Initial value of the message
                .doOnNext(s -> log.info("Message is {}", s))
                // Returns a Mono that completes without emitting any item
                .flatMap(s -> Mono.empty())
                // Will not execute because there's no data available to display
                .doOnNext(s -> log.info("Message is {}", s))
                // The final value of the message will be null
                .doOnSuccess(s -> log.info("Success Listener executed!! Message is {}", s));


        // Flux: onSubscribe -> request (unbounded) -> onNext -> onComplete
        mono.subscribe(s -> log.info("Message is {}", s),
                Throwable::printStackTrace, () -> log.info("Completed!!"));

        log.info("\n");
    }

    @Test
    public void monoSubscriberErrorListeners() {
        Mono<Object> monoException = Mono.error(new Exception("Exception thrown"))
                .doOnError(e -> log.error("Error message is {}", e.getMessage()))
                // Will not execute because the exception was thrown
                .doOnNext(value -> log.info("Starting next execution ..."))
                .log();

        StepVerifier.create(monoException)
                .expectError(Exception.class)
                .verify();
    }

    @Test
    public void monoSubscriberErrorResumeListeners() {
        final String message = "Test message :P";
        Mono<Object> monoException = Mono.error(new Exception("Exception thrown"))
                .onErrorResume(value -> {
                    log.info("onErrorResume method");
                    return Mono.just(message);
                })
                .doOnError(e -> log.error("Error message is {}", e.getMessage()))
                .log();

        StepVerifier.create(monoException)
                .expectNext(message)
                .verifyComplete();
    }

    @Test
    public void monoSubscriberErrorReturnListeners() {
        final String message = "Test message :P";
        Mono<Object> monoException = Mono.error(new Exception("Exception thrown"))
                .onErrorResume(value -> {
                    log.info("onErrorResume method");
                    return Mono.just(message);
                })
                .onErrorReturn(message)
                .doOnError(e -> log.error("Error message is {}", e.getMessage()))
                .log();

        StepVerifier.create(monoException)
                .expectNext(message)
                .verifyComplete();
    }
}

package com.uno_restart;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.util.concurrent.TimeUnit;


@SpringBootTest(classes = {DefaultTests.class})
class DefaultTests {
    @Test
    void flux_interval() throws InterruptedException {
        Flux.interval(Duration.ofSeconds(1))
                .map(input -> {
                    if (input < 3) return "tick " + input;
                    throw new RuntimeException("boom");
                })
                .onErrorReturn("Uh oh")
                .subscribe(System.out::println);

        TimeUnit.SECONDS.sleep(5);
    }

    record demo(String name, int age) {
        public static void foo(){
            System.out.println("foo");
        }
    }
}

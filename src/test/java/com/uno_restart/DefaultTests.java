package com.uno_restart;

import com.google.common.hash.Hashing;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import reactor.core.publisher.Flux;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.UUID;
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

    @Test
    void hash(){
        String demo = Hashing.sha256().hashString("", StandardCharsets.UTF_8).toString();
        System.out.println(demo.length());
        System.out.println(demo);
    }

    @Test
    void UUID() {
        for (int i = 0; i < 5; i++) {
            System.out.println(UUID.randomUUID());
        }
    }

    @Test
    public void foo() {
        Integer i = 10;
        i += 1;
        System.out.println(i);
    }
}

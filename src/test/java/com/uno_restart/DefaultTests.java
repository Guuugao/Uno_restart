package com.uno_restart;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.uno_restart.types.player.PlayerInfo;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;


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

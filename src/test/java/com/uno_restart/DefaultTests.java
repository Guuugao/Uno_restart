package com.uno_restart;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
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
    public void foo() {
        Map<Integer, String> map = new TreeMap<>();
        Collection<String> values = map.values();
        map.put(1, "a");
        map.put(2, "b");
        map.put(3, "c");
        map.put(4, "da");
        map.put(5, "db");
        System.out.println("map: " + map);
        System.out.println("values: " + values);
    }
}

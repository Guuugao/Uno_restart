package com.uno_restart;

import cn.dev33.satoken.stp.StpUtil;
import com.google.common.hash.Hashing;
import com.uno_restart.types.game.GameCard;
import com.uno_restart.types.player.PlayerInfo;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import reactor.core.publisher.Flux;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.*;
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
            System.out.println(UUID.randomUUID().toString());
        }
    }

    @Test
    public void foo() {
        Integer i = 10;
        i += 1;
        System.out.println(i);
    }
}

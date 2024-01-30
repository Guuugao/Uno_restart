package com.uno_restart;

import com.uno_restart.types.enums.EnumUnoCardColor;
import com.uno_restart.types.enums.EnumUnoCardType;
import com.uno_restart.types.game.GameCard;
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

    @Test
    public void foo(){
        List<PlayerInfo> list = List.of(new PlayerInfo("1", "1", "1"));
        Map<String, PlayerInfo> map = list.stream().collect(Collectors.toMap(PlayerInfo::getPlayerName, Function.identity()));
        map.get("1").setPassword("2");
        System.out.println(list);
        System.out.println(map);
    }
}

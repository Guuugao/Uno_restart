package com.uno_restart;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;


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
    void foo() throws InterruptedException {
        Future<Integer> future = new CompletableFuture<>();
        Flux<String> flux = Flux.generate(
                () -> 0, // 初始state值
                (state, sink) -> {
                    sink.next("3 x " + state + " = " + 3 * state); // 产生数据是同步的，每次产生一个数据
                    if (state == 10) {
                        sink.complete();
                    }
                    return state + 1; // 改变状态
                },
                (state) -> System.out.println("state: " + state)); // 最后状态值
        // 订阅时触发requset->sink.next顺序产生数据
        // 生产一个数据消费一个
        flux.subscribe(System.out::println);

        TimeUnit.SECONDS.sleep(5);
    }

    Consumer<String> producer;

    @Test
    public void testCreate() throws InterruptedException {
    }
}

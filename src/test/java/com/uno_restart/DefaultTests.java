package com.uno_restart;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.uno_restart.exception.PlayerAbnormalException;
import com.uno_restart.exception.UnoException;
import com.uno_restart.types.enums.EnumUnoCardColor;
import com.uno_restart.types.enums.EnumUnoCardType;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.TimeUnit;


@SpringBootTest(classes = {DefaultTests.class})
class DefaultTests {
    ObjectMapper jsonParser = new ObjectMapper();

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

    public static class base {
        @NotNull
        private final EnumUnoCardType cardType;
        @NotNull
        private final EnumUnoCardColor cardColor;
        private final int cardID;

        public base(@NotNull EnumUnoCardType cardType, @NotNull EnumUnoCardColor cardColor, int cardID) {
            this.cardType = cardType;
            this.cardColor = cardColor;
            this.cardID = cardID;
        }

        @NotNull
        public EnumUnoCardType cardType() {
            return cardType;
        }

        @NotNull
        public EnumUnoCardColor cardColor() {
            return cardColor;
        }

        public int cardID() {
            return cardID;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) return true;
            if (obj == null || obj.getClass() != this.getClass()) return false;
            var that = (base) obj;
            return Objects.equals(this.cardType, that.cardType) &&
                    Objects.equals(this.cardColor, that.cardColor) &&
                    this.cardID == that.cardID;
        }

        @Override
        public int hashCode() {
            return Objects.hash(cardType, cardColor, cardID);
        }

        @Override
        public String toString() {
            return "GameCardInput[" +
                    "cardType=" + cardType + ", " +
                    "cardColor=" + cardColor + ", " +
                    "cardID=" + cardID + ']';
        }

    }

    public static final class extend extends base {
        public extend(@NotNull EnumUnoCardType cardType, @NotNull EnumUnoCardColor cardColor, int cardID) {
            super(cardType, cardColor, cardID);
        }

        @Override
        public boolean equals(Object obj) {
            return obj == this || obj != null && obj.getClass() == this.getClass();
        }

        @Override
        public int hashCode() {
            return 1;
        }

        @Override
        public String toString() {
            return "GameCard[]";
        }
    }

    @Test
    public void foo() throws JsonProcessingException {
        Throwable exception = new PlayerAbnormalException("123");
        System.out.println(exception instanceof UnoException);
    }
}

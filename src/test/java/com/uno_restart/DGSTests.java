package com.uno_restart;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.TypeRef;
import com.netflix.graphql.dgs.DgsQueryExecutor;
import com.netflix.graphql.dgs.client.WebSocketGraphQLClient;
import com.netflix.graphql.dgs.client.codegen.GraphQLQueryRequest;
import com.uno_restart.generated.client.PlayerLoginGraphQLQuery;
import com.uno_restart.generated.client.PlayerLoginProjectionRoot;
import org.intellij.lang.annotations.Language;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.reactive.socket.client.ReactorNettyWebSocketClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SpringBootTest
public class DGSTests {

    @Autowired
    DgsQueryExecutor dgsQueryExecutor;
    ObjectMapper objectMapper = new ObjectMapper();
    private WebSocketGraphQLClient webSocketGraphQLClient;
    @BeforeEach
    public void setup() {
        int port = 10000;
        webSocketGraphQLClient = new WebSocketGraphQLClient("ws://localhost:" + port + "/subscriptions",
                new ReactorNettyWebSocketClient());
    }

    @Test
    public void subscription() {
        @Language("graphql") String subscriptionRequest = "subscription { hello }";
        Flux<String> hello = webSocketGraphQLClient.reactiveExecuteQuery(
                subscriptionRequest, Collections.emptyMap())
                .take(3)
                .map(r -> r.extractValue("$.data.hello"));
        StepVerifier.create(hello)
                .expectNext("hello")
                .expectNext(" ")
                .expectNext("world")
                .thenCancel()
                .verify();
    }

    @Test
    void query() {
        GraphQLQueryRequest request = new GraphQLQueryRequest(
                PlayerLoginGraphQLQuery.newRequest()
                        .playerName("admin")
                        .password("admin0000")
                        .build(),
                new PlayerLoginProjectionRoot<>().success().message()
        );

        List<String> feedback = dgsQueryExecutor.executeAndExtractJsonPath(
                request.serialize()
                , "$.data.playerLogin[*]");

        System.out.println(feedback);
    }

    @Test
    void upload() throws IOException {
        login("admin", "admin0000");

        @Language("GraphQL") String mutation = "mutation ($avatar: Upload!) { playerAvatarModify(avatar: $avatar) { success message avatarPath }}";
        Map<String, Object> map = new HashMap<>() {{
            put("avatar", new MockMultipartFile("test", "test.png", "image/jpeg",
                    new FileInputStream("C:\\Users\\TIME LEAP MACHINE\\Pictures\\插画\\正经\\モ誰 2023-02-23\\75日目,マキマ2022-07-0199435243_p0.jpg")));
        }};

        List<String> feedback = dgsQueryExecutor.executeAndExtractJsonPathAsObject(
                mutation,
                "$.data.playerAvatarModify[*]",
                map,
                new TypeRef<>() {
                }
        );

        System.out.println(feedback);

    }

    void login(String playerName, String password) {
        GraphQLQueryRequest request = new GraphQLQueryRequest(
                PlayerLoginGraphQLQuery.newRequest()
                        .playerName(playerName)
                        .password(password)
                        .build(),
                new PlayerLoginProjectionRoot<>().success().message()
        );

        dgsQueryExecutor.executeAndExtractJsonPath(
                request.serialize(),
                "$.data.playerLogin[*]"
        );
    }
}
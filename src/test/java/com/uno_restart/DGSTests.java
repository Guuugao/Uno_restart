package com.uno_restart;

import com.netflix.graphql.dgs.DgsQueryExecutor;
import com.netflix.graphql.dgs.client.codegen.GraphQLQueryRequest;
import com.uno_restart.generated.client.PlayerLoginGraphQLQuery;
import com.uno_restart.generated.client.PlayerLoginProjectionRoot;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

@SpringBootTest
public class DGSTests {

    @Autowired
    DgsQueryExecutor dgsQueryExecutor;

    @Test
    void dgsTest() {
        GraphQLQueryRequest graphQLQueryRequest = new GraphQLQueryRequest(
                PlayerLoginGraphQLQuery.newRequest()
                        .playerName("admin")
                        .password("root")
                        .build(),
                new PlayerLoginProjectionRoot<>().success().message()
        );

        List<String> res = dgsQueryExecutor.executeAndExtractJsonPath(
                graphQLQueryRequest.serialize()
                , "$.data.playerLogin[*]");

        System.out.println(res);
    }
}
package com.uno_restart.handler;

import com.netflix.graphql.dgs.DgsComponent;
import com.netflix.graphql.dgs.exceptions.DefaultDataFetcherExceptionHandler;
import com.netflix.graphql.types.errors.ErrorType;
import com.netflix.graphql.types.errors.TypedGraphQLError;
import com.uno_restart.exception.UnoException;
import graphql.GraphQLError;
import graphql.execution.DataFetcherExceptionHandler;
import graphql.execution.DataFetcherExceptionHandlerParameters;
import graphql.execution.DataFetcherExceptionHandlerResult;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Slf4j
@DgsComponent
public class UnoExceptionHandler implements DataFetcherExceptionHandler {
    private final DefaultDataFetcherExceptionHandler defaultHandler = new DefaultDataFetcherExceptionHandler();

    @Override
    public CompletableFuture<DataFetcherExceptionHandlerResult> handleException(DataFetcherExceptionHandlerParameters handlerParameters) {
        Throwable e = handlerParameters.getException().getCause();
        if (e instanceof UnoException) {
            Map<String, Object> debugInfo = new HashMap<>();
            debugInfo.put("exception type", e.getClass()); // 大致告知异常范围为玩家/房间/游戏之一
            GraphQLError graphqlError = TypedGraphQLError
                    .newBuilder()
                    .errorType(ErrorType.INTERNAL)
                    .debugInfo(debugInfo)
                    .message(e.getMessage())
                    .path(handlerParameters.getPath()).build();

            return CompletableFuture.completedFuture(
                    DataFetcherExceptionHandlerResult
                            .newResult()
                            .error(graphqlError)
                            .build());
        } else {
            return defaultHandler.handleException(handlerParameters);
        }
    }
}

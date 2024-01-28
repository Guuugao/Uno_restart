//package com.uno_restart.handler;
//
//import cn.dev33.satoken.exception.NotLoginException;
//import com.netflix.graphql.dgs.exceptions.DefaultDataFetcherExceptionHandler;
//import com.netflix.graphql.types.errors.TypedGraphQLError;
//import com.uno_restart.exception.RoomNotExistsException;
//import graphql.GraphQLError;
//import graphql.execution.DataFetcherExceptionHandler;
//import graphql.execution.DataFetcherExceptionHandlerParameters;
//import graphql.execution.DataFetcherExceptionHandlerResult;
//import org.springframework.stereotype.Component;
//
//import java.util.concurrent.CompletableFuture;
//
//@Component
//public class UnoDataFetchingExceptionHandler implements DataFetcherExceptionHandler {
//    private final DataFetcherExceptionHandler defaultHandler = new DefaultDataFetcherExceptionHandler();
//
//    @Override
//    public CompletableFuture<DataFetcherExceptionHandlerResult> handleException(DataFetcherExceptionHandlerParameters handlerParameters) {
//        Throwable exception = handlerParameters.getException();
//        if (exception instanceof NotLoginException) {
//            GraphQLError graphqlError = TypedGraphQLError
//                    .newInternalErrorBuilder()
//                    .message(exception.getMessage())
//                    .path(handlerParameters.getPath())
//                    .build();
//
//            DataFetcherExceptionHandlerResult result = DataFetcherExceptionHandlerResult
//                    .newResult()
//                    .error(graphqlError)
//                    .build();
//
//            return CompletableFuture.completedFuture(result);
//        }else{ // 默认处理
//            return defaultHandler.handleException(handlerParameters);
//        }
//    }
//}

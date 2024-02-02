package com.uno_restart.exception;

import graphql.ErrorClassification;
import graphql.GraphQLError;
import graphql.language.SourceLocation;

import java.util.List;

public class UnoException extends Exception {
    public UnoException(String message) {
        super(message);
    }
}

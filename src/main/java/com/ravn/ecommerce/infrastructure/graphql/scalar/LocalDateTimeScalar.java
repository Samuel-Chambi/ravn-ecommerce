package com.ravn.ecommerce.infrastructure.graphql.scalar;

import graphql.language.StringValue;
import graphql.schema.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Custom GraphQL scalar that serializes/deserializes {@link LocalDateTime}
 * as an ISO-8601 String (e.g. "2024-03-15T10:30:00").
 */
public class LocalDateTimeScalar {

    private LocalDateTimeScalar() {
    }

    public static final GraphQLScalarType INSTANCE = GraphQLScalarType.newScalar()
            .name("LocalDateTime")
            .description("ISO-8601 LocalDateTime scalar (e.g. 2024-03-15T10:30:00)")
            .coercing(new Coercing<LocalDateTime, String>() {

                @Override
                public String serialize(Object dataFetcherResult) throws CoercingSerializeException {
                    if (dataFetcherResult instanceof LocalDateTime ldt) {
                        return ldt.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
                    }
                    throw new CoercingSerializeException("Expected LocalDateTime, got: " + dataFetcherResult);
                }

                @Override
                public LocalDateTime parseValue(Object input) throws CoercingParseValueException {
                    if (input instanceof String s) {
                        return LocalDateTime.parse(s, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
                    }
                    throw new CoercingParseValueException("Expected String for LocalDateTime, got: " + input);
                }

                @Override
                public LocalDateTime parseLiteral(Object ast) throws CoercingParseLiteralException {
                    if (ast instanceof StringValue sv) {
                        return LocalDateTime.parse(sv.getValue(), DateTimeFormatter.ISO_LOCAL_DATE_TIME);
                    }
                    throw new CoercingParseLiteralException("Expected StringValue for LocalDateTime");
                }
            })
            .build();
}

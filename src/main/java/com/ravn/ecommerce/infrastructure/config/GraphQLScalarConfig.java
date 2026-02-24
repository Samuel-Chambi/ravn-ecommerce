package com.ravn.ecommerce.infrastructure.config;

import com.ravn.ecommerce.infrastructure.graphql.scalar.LocalDateTimeScalar;
import graphql.scalars.ExtendedScalars;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.graphql.execution.RuntimeWiringConfigurer;

@Configuration
public class GraphQLScalarConfig {

    /**
     * Registers custom scalars so Spring for GraphQL can correctly
     * serialize/deserialize
     * BigDecimal and LocalDateTime used in our response DTOs.
     *
     * BigDecimal → graphql-java-extended-scalars (serialises as a String number)
     * LocalDateTime → custom scalar (ISO-8601 String)
     */
    @Bean
    public RuntimeWiringConfigurer runtimeWiringConfigurer() {
        return wiringBuilder -> wiringBuilder
                .scalar(ExtendedScalars.GraphQLBigDecimal)
                .scalar(LocalDateTimeScalar.INSTANCE);
    }
}

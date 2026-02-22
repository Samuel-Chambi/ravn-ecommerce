package com.ravn.ecommerce.application.useCases;

public interface UseCase<I, O> {
    O execute(I input);
}

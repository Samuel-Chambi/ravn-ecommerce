package com.ravn.ecommerce.application.usecases;

public interface UseCase<I, O> {
    O execute(I input);
}

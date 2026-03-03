package com.ravn.ecommerce.application.usecases.address;

import com.ravn.ecommerce.application.repositories.AddressRepository;
import com.ravn.ecommerce.application.repositories.UserRepository;
import com.ravn.ecommerce.application.usecases.address.command.DeleteAddressCommand;
import com.ravn.ecommerce.domain.exceptions.EntityNotFoundException;
import com.ravn.ecommerce.domain.exceptions.UserNotFound;
import com.ravn.ecommerce.domain.model.user.Address;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DeleteUserAddressUseCaseTest {

    @Mock
    private AddressRepository addressRepository;
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private DeleteUserAddressUseCase useCase;

    private static final Long USER_ID = 1L;
    private static final Long ADDRESS_ID = 10L;

    @Test
    @DisplayName("Should delete address successfully")
    void shouldDeleteAddressSuccessfully() {
        DeleteAddressCommand command = new DeleteAddressCommand(USER_ID, ADDRESS_ID);
        Address address = Address.builder()
                .id(ADDRESS_ID)
                .userId(USER_ID)
                .fullName("John Doe")
                .createdAt(LocalDateTime.now())
                .build();

        when(userRepository.existsById(USER_ID)).thenReturn(true);
        when(addressRepository.findByIdAndUserId(ADDRESS_ID, USER_ID)).thenReturn(Optional.of(address));

        useCase.execute(command);

        verify(addressRepository).deleteById(ADDRESS_ID);
    }

    @Test
    @DisplayName("Should throw when user does not exist")
    void shouldThrowWhenUserNotFound() {
        DeleteAddressCommand command = new DeleteAddressCommand(USER_ID, ADDRESS_ID);

        when(userRepository.existsById(USER_ID)).thenReturn(false);

        assertThatThrownBy(() -> useCase.execute(command))
                .isInstanceOf(UserNotFound.class);
    }

    @Test
    @DisplayName("Should throw when address not found for user")
    void shouldThrowWhenAddressNotFound() {
        DeleteAddressCommand command = new DeleteAddressCommand(USER_ID, ADDRESS_ID);

        when(userRepository.existsById(USER_ID)).thenReturn(true);
        when(addressRepository.findByIdAndUserId(ADDRESS_ID, USER_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(command))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Address ID");
    }
}

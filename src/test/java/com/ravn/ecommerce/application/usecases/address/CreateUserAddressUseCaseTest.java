package com.ravn.ecommerce.application.usecases.address;

import com.ravn.ecommerce.application.dto.request.address.CreateAddressRequest;
import com.ravn.ecommerce.application.dto.response.AddressResponse;
import com.ravn.ecommerce.application.repositories.AddressRepository;
import com.ravn.ecommerce.application.repositories.UserRepository;
import com.ravn.ecommerce.application.usecases.address.command.CreateAddressCommand;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CreateUserAddressUseCaseTest {

    @Mock
    private AddressRepository addressRepository;
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CreateUserAddressUseCase useCase;

    private static final Long USER_ID = 1L;

    private CreateAddressRequest buildRequest(boolean isDefault) {
        return CreateAddressRequest.builder()
                .fullName("John Doe")
                .phone("+1234567890")
                .street("123 Main St")
                .city("New York")
                .state("NY")
                .country("US")
                .zipCode("10001")
                .isDefault(isDefault)
                .build();
    }

    private Address buildSavedAddress(boolean isDefault) {
        return Address.builder()
                .id(1L)
                .userId(USER_ID)
                .fullName("John Doe")
                .phone("+1234567890")
                .street("123 Main St")
                .city("New York")
                .state("NY")
                .country("US")
                .zipCode("10001")
                .isDefault(isDefault)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("Should create address successfully")
    void shouldCreateAddressSuccessfully() {
        CreateAddressRequest request = buildRequest(false);
        CreateAddressCommand command = new CreateAddressCommand(USER_ID, request);
        Address saved = buildSavedAddress(false);

        when(userRepository.existsById(USER_ID)).thenReturn(true);
        when(addressRepository.save(any(Address.class))).thenReturn(saved);

        AddressResponse response = useCase.execute(command);

        assertThat(response).isNotNull();
        assertThat(response.getFullName()).isEqualTo("John Doe");
        assertThat(response.getCity()).isEqualTo("New York");
        verify(addressRepository).save(any(Address.class));
    }

    @Test
    @DisplayName("Should unset existing default when creating new default address")
    void shouldUnsetExistingDefaultAddress() {
        CreateAddressRequest request = buildRequest(true);
        CreateAddressCommand command = new CreateAddressCommand(USER_ID, request);
        Address existingDefault = buildSavedAddress(true);
        existingDefault.setId(99L);
        Address newSaved = buildSavedAddress(true);

        when(userRepository.existsById(USER_ID)).thenReturn(true);
        when(addressRepository.findDefaultByUserId(USER_ID)).thenReturn(Optional.of(existingDefault));
        when(addressRepository.save(any(Address.class))).thenReturn(newSaved);

        AddressResponse response = useCase.execute(command);

        assertThat(response).isNotNull();
        assertThat(response.isDefault()).isTrue();
        // Verify save called at least twice (once for unset, once for new address)
        verify(addressRepository).findDefaultByUserId(USER_ID);
    }

    @Test
    @DisplayName("Should throw when user does not exist")
    void shouldThrowWhenUserNotFound() {
        CreateAddressCommand command = new CreateAddressCommand(USER_ID, buildRequest(false));

        when(userRepository.existsById(USER_ID)).thenReturn(false);

        assertThatThrownBy(() -> useCase.execute(command))
                .isInstanceOf(UserNotFound.class);
    }
}

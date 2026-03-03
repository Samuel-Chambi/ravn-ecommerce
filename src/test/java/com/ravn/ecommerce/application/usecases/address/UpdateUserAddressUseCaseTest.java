package com.ravn.ecommerce.application.usecases.address;

import com.ravn.ecommerce.application.dto.request.address.UpdateAddressRequest;
import com.ravn.ecommerce.application.dto.response.AddressResponse;
import com.ravn.ecommerce.application.repositories.AddressRepository;
import com.ravn.ecommerce.application.repositories.UserRepository;
import com.ravn.ecommerce.application.usecases.address.command.UpdateAddressCommand;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UpdateUserAddressUseCaseTest {

    @Mock
    private AddressRepository addressRepository;
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UpdateUserAddressUseCase useCase;

    private static final Long USER_ID = 1L;
    private static final Long ADDRESS_ID = 10L;

    private Address buildAddress() {
        return Address.builder()
                .id(ADDRESS_ID)
                .userId(USER_ID)
                .fullName("John Doe")
                .phone("+1234567890")
                .street("123 Main St")
                .city("New York")
                .state("NY")
                .country("US")
                .zipCode("10001")
                .isDefault(false)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("Should update address fields successfully")
    void shouldUpdateAddressSuccessfully() {
        Address address = buildAddress();
        UpdateAddressRequest request = UpdateAddressRequest.builder()
                .city("Los Angeles")
                .state("CA")
                .build();
        UpdateAddressCommand command = new UpdateAddressCommand(USER_ID, ADDRESS_ID, request);

        when(userRepository.existsById(USER_ID)).thenReturn(true);
        when(addressRepository.findByIdAndUserId(ADDRESS_ID, USER_ID)).thenReturn(Optional.of(address));
        when(addressRepository.save(any(Address.class))).thenReturn(address);

        AddressResponse response = useCase.execute(command);

        assertThat(response).isNotNull();
        assertThat(response.getCity()).isEqualTo("Los Angeles");
        assertThat(response.getState()).isEqualTo("CA");
        assertThat(response.getFullName()).isEqualTo("John Doe"); // unchanged
    }

    @Test
    @DisplayName("Should set as default and unset previous default")
    void shouldSetAsDefaultAndUnsetPrevious() {
        Address address = buildAddress();
        Address existingDefault = Address.builder()
                .id(99L).userId(USER_ID).isDefault(true)
                .createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now())
                .build();
        UpdateAddressRequest request = UpdateAddressRequest.builder().isDefault(true).build();
        UpdateAddressCommand command = new UpdateAddressCommand(USER_ID, ADDRESS_ID, request);

        when(userRepository.existsById(USER_ID)).thenReturn(true);
        when(addressRepository.findByIdAndUserId(ADDRESS_ID, USER_ID)).thenReturn(Optional.of(address));
        when(addressRepository.findDefaultByUserId(USER_ID)).thenReturn(Optional.of(existingDefault));
        when(addressRepository.save(any(Address.class))).thenReturn(address);

        AddressResponse response = useCase.execute(command);

        assertThat(response).isNotNull();
        verify(addressRepository).findDefaultByUserId(USER_ID);
    }

    @Test
    @DisplayName("Should throw when user does not exist")
    void shouldThrowWhenUserNotFound() {
        UpdateAddressRequest request = UpdateAddressRequest.builder().city("LA").build();
        UpdateAddressCommand command = new UpdateAddressCommand(USER_ID, ADDRESS_ID, request);

        when(userRepository.existsById(USER_ID)).thenReturn(false);

        assertThatThrownBy(() -> useCase.execute(command))
                .isInstanceOf(UserNotFound.class);
    }

    @Test
    @DisplayName("Should throw when address not found for user")
    void shouldThrowWhenAddressNotFound() {
        UpdateAddressRequest request = UpdateAddressRequest.builder().city("LA").build();
        UpdateAddressCommand command = new UpdateAddressCommand(USER_ID, ADDRESS_ID, request);

        when(userRepository.existsById(USER_ID)).thenReturn(true);
        when(addressRepository.findByIdAndUserId(ADDRESS_ID, USER_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(command))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Address ID");
    }
}

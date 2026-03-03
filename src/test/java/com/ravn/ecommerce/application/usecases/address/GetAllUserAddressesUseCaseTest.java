package com.ravn.ecommerce.application.usecases.address;

import com.ravn.ecommerce.application.dto.response.AddressResponse;
import com.ravn.ecommerce.application.repositories.AddressRepository;
import com.ravn.ecommerce.application.repositories.UserRepository;
import com.ravn.ecommerce.domain.exceptions.UserNotFound;
import com.ravn.ecommerce.domain.model.user.Address;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetAllUserAddressesUseCaseTest {

    @Mock
    private AddressRepository addressRepository;
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private GetAllUserAddressesUseCase useCase;

    private static final Long USER_ID = 1L;

    private Address buildAddress(Long id, String city) {
        return Address.builder()
                .id(id)
                .userId(USER_ID)
                .fullName("John Doe")
                .phone("+1234567890")
                .street("123 Main St")
                .city(city)
                .state("NY")
                .country("US")
                .zipCode("10001")
                .isDefault(false)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("Should return all addresses for user")
    void shouldReturnAllAddresses() {
        List<Address> addresses = List.of(
                buildAddress(1L, "New York"),
                buildAddress(2L, "Los Angeles")
        );

        when(userRepository.existsById(USER_ID)).thenReturn(true);
        when(addressRepository.findAllByUserId(USER_ID)).thenReturn(addresses);

        List<AddressResponse> responses = useCase.execute(USER_ID);

        assertThat(responses).hasSize(2);
        assertThat(responses.get(0).getCity()).isEqualTo("New York");
        assertThat(responses.get(1).getCity()).isEqualTo("Los Angeles");
    }

    @Test
    @DisplayName("Should return empty list when user has no addresses")
    void shouldReturnEmptyListWhenNoAddresses() {
        when(userRepository.existsById(USER_ID)).thenReturn(true);
        when(addressRepository.findAllByUserId(USER_ID)).thenReturn(List.of());

        List<AddressResponse> responses = useCase.execute(USER_ID);

        assertThat(responses).isEmpty();
    }

    @Test
    @DisplayName("Should throw when user does not exist")
    void shouldThrowWhenUserNotFound() {
        when(userRepository.existsById(USER_ID)).thenReturn(false);

        assertThatThrownBy(() -> useCase.execute(USER_ID))
                .isInstanceOf(UserNotFound.class);
    }
}

package com.ravn.ecommerce.application.usecases.address;

import com.ravn.ecommerce.application.dto.response.AddressResponse;
import com.ravn.ecommerce.application.repositories.AddressRepository;
import com.ravn.ecommerce.application.repositories.UserRepository;
import com.ravn.ecommerce.application.usecases.UseCase;
import com.ravn.ecommerce.application.usecases.address.command.CreateAddressCommand;
import com.ravn.ecommerce.domain.exceptions.UserNotFound;
import com.ravn.ecommerce.domain.model.user.Address;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@Slf4j
@RequiredArgsConstructor
public class CreateUserAddressUseCase implements UseCase<CreateAddressCommand, AddressResponse> {
    private final AddressRepository addressRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public AddressResponse execute(CreateAddressCommand command) {
        log.info("Creating address for user ID: {}", command.userId());
        if (!userRepository.existsById(command.userId())) {
            throw new UserNotFound(String.format("User ID %d does not exist", command.userId()));
        }

        // If new address is default, un-set current default
        if (command.request().isDefault()) {
            addressRepository.findDefaultByUserId(command.userId()).ifPresent(existing -> {
                existing.unsetDefault();
                existing.setUpdatedAt(LocalDateTime.now());
                addressRepository.save(existing);
            });
        }

        Address address = Address.builder()
                .userId(command.userId())
                .fullName(command.request().getFullName())
                .phone(command.request().getPhone())
                .street(command.request().getStreet())
                .city(command.request().getCity())
                .state(command.request().getState())
                .country(command.request().getCountry())
                .zipCode(command.request().getZipCode())
                .isDefault(command.request().isDefault())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        Address saved = addressRepository.save(address);
        log.info("Address ID {} created for user ID {}", saved.getId(), command.userId());
        return AddressResponse.toDto(saved);
    }
}

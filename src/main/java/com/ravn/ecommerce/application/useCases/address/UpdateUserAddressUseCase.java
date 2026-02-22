package com.ravn.ecommerce.application.usecases.address;

import com.ravn.ecommerce.application.dto.response.AddressResponse;
import com.ravn.ecommerce.application.repositories.AddressRepository;
import com.ravn.ecommerce.application.repositories.UserRepository;
import com.ravn.ecommerce.application.usecases.UseCase;
import com.ravn.ecommerce.application.usecases.address.command.UpdateAddressCommand;
import com.ravn.ecommerce.domain.exceptions.EntityNotFoundException;
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
public class UpdateUserAddressUseCase implements UseCase<UpdateAddressCommand, AddressResponse> {
    private final AddressRepository addressRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public AddressResponse execute(UpdateAddressCommand command) {
        log.info("Updating address ID {} for user ID {}", command.addressId(), command.userId());
        if (!userRepository.existsById(command.userId())) {
            throw new UserNotFound(String.format("User ID %d does not exist", command.userId()));
        }
        Address address = addressRepository.findByIdAndUserId(command.addressId(), command.userId())
                .orElseThrow(() -> new EntityNotFoundException(
                        String.format("Address ID %d not found for user ID %d", command.addressId(),
                                command.userId())));

        var req = command.request();

        // Apply only non-null fields
        if (req.getFullName() != null)
            address.setFullName(req.getFullName());
        if (req.getPhone() != null)
            address.setPhone(req.getPhone());
        if (req.getStreet() != null)
            address.setStreet(req.getStreet());
        if (req.getCity() != null)
            address.setCity(req.getCity());
        if (req.getState() != null)
            address.setState(req.getState());
        if (req.getCountry() != null)
            address.setCountry(req.getCountry());
        if (req.getZipCode() != null)
            address.setZipCode(req.getZipCode());

        // Handle default flag change
        if (Boolean.TRUE.equals(req.getIsDefault()) && !address.isDefault()) {
            addressRepository.findDefaultByUserId(command.userId()).ifPresent(existing -> {
                existing.unsetDefault();
                existing.setUpdatedAt(LocalDateTime.now());
                addressRepository.save(existing);
            });
            address.setAsDefault();
        } else if (Boolean.FALSE.equals(req.getIsDefault())) {
            address.unsetDefault();
        }

        address.setUpdatedAt(LocalDateTime.now());
        Address updated = addressRepository.save(address);
        log.info("Address ID {} updated for user ID {}", updated.getId(), command.userId());
        return AddressResponse.toDto(updated);
    }
}

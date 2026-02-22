package com.ravn.ecommerce.application.usecases.address;

import com.ravn.ecommerce.application.repositories.AddressRepository;
import com.ravn.ecommerce.application.repositories.UserRepository;
import com.ravn.ecommerce.application.usecases.UseCase;
import com.ravn.ecommerce.application.usecases.address.command.DeleteAddressCommand;
import com.ravn.ecommerce.domain.exceptions.EntityNotFoundException;
import com.ravn.ecommerce.domain.exceptions.UserNotFound;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class DeleteUserAddressUseCase implements UseCase<DeleteAddressCommand, Void> {
    private final AddressRepository addressRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public Void execute(DeleteAddressCommand command) {
        log.info("Deleting address ID {} for user ID {}", command.addressId(), command.userId());
        if (!userRepository.existsById(command.userId())) {
            throw new UserNotFound(String.format("User ID %d does not exist", command.userId()));
        }
        if (addressRepository.findByIdAndUserId(command.addressId(), command.userId()).isEmpty()) {
            throw new EntityNotFoundException(
                    String.format("Address ID %d not found for user ID %d", command.addressId(), command.userId()));
        }
        addressRepository.deleteById(command.addressId());
        log.info("Address ID {} deleted for user ID {}", command.addressId(), command.userId());
        return null;
    }
}

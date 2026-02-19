package com.ravn.ecommerce.application.usecases.address;

import com.ravn.ecommerce.application.dto.response.AddressResponse;
import com.ravn.ecommerce.application.repositories.AddressRepository;
import com.ravn.ecommerce.application.repositories.UserRepository;
import com.ravn.ecommerce.application.usecases.UseCase;
import com.ravn.ecommerce.domain.exceptions.UserNotFound;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class GetAllUserAddressesUseCase implements UseCase<Long, List<AddressResponse>> {
    private final AddressRepository addressRepository;
    private final UserRepository userRepository;

    @Override
    public List<AddressResponse> execute(Long userId) {
        log.info("Fetching addresses for user ID: {}", userId);
        if (!userRepository.existsById(userId)) {
            throw new UserNotFound(String.format("User ID %d does not exist", userId));
        }
        return addressRepository.findAllByUserId(userId).stream()
                .map(AddressResponse::toDto)
                .toList();
    }
}

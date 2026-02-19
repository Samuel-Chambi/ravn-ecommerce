package com.ravn.ecommerce.presentation.rest.controllers;

import com.ravn.ecommerce.application.dto.request.address.CreateAddressRequest;
import com.ravn.ecommerce.application.dto.request.address.UpdateAddressRequest;
import com.ravn.ecommerce.application.dto.response.AddressResponse;
import com.ravn.ecommerce.application.services.CurrentUserService;
import com.ravn.ecommerce.application.usecases.address.*;
import com.ravn.ecommerce.application.usecases.address.command.CreateAddressCommand;
import com.ravn.ecommerce.application.usecases.address.command.DeleteAddressCommand;
import com.ravn.ecommerce.application.usecases.address.command.UpdateAddressCommand;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/addresses")
@RequiredArgsConstructor
public class AddressController {

    private final CreateUserAddressUseCase createUserAddressUseCase;
    private final GetAllUserAddressesUseCase getAllUserAddressesUseCase;
    private final UpdateUserAddressUseCase updateUserAddressUseCase;
    private final DeleteUserAddressUseCase deleteUserAddressUseCase;
    private final CurrentUserService currentUserService;

    @PostMapping
    public ResponseEntity<AddressResponse> createAddress(
            @RequestBody @Valid CreateAddressRequest request) {
        Long userId = currentUserService.getCurrentUserId();
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(createUserAddressUseCase.execute(new CreateAddressCommand(userId, request)));
    }

    @GetMapping
    public ResponseEntity<List<AddressResponse>> getAllAddresses() {
        Long userId = currentUserService.getCurrentUserId();
        return ResponseEntity.ok(getAllUserAddressesUseCase.execute(userId));
    }

    @PutMapping("/{addressId}")
    public ResponseEntity<AddressResponse> updateAddress(
            @PathVariable Long addressId,
            @RequestBody UpdateAddressRequest request) {
        Long userId = currentUserService.getCurrentUserId();
        return ResponseEntity
                .ok(updateUserAddressUseCase.execute(new UpdateAddressCommand(userId, addressId, request)));
    }

    @DeleteMapping("/{addressId}")
    public ResponseEntity<Void> deleteAddress(@PathVariable Long addressId) {
        Long userId = currentUserService.getCurrentUserId();
        deleteUserAddressUseCase.execute(new DeleteAddressCommand(userId, addressId));
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}

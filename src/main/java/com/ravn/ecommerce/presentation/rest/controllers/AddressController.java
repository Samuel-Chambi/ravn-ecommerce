package com.ravn.ecommerce.presentation.rest.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

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
@Tag(name = "Address Management", description = "Endpoints for managing user delivery addresses")
public class AddressController {

    private final CreateUserAddressUseCase createUserAddressUseCase;
    private final GetAllUserAddressesUseCase getAllUserAddressesUseCase;
    private final UpdateUserAddressUseCase updateUserAddressUseCase;
    private final DeleteUserAddressUseCase deleteUserAddressUseCase;
    private final CurrentUserService currentUserService;

    @Operation(summary = "Create an address", description = "Creates a new delivery address for the currently authenticated user. This address can later be used during checkout.")
    @PostMapping
    public ResponseEntity<AddressResponse> createAddress(
            @RequestBody @Valid CreateAddressRequest request) {
        Long userId = currentUserService.getCurrentUserId();
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(createUserAddressUseCase.execute(new CreateAddressCommand(userId, request)));
    }

    @Operation(summary = "Get all addresses", description = "Retrieves a list of all delivery addresses associated with the currently authenticated user.")
    @GetMapping
    public ResponseEntity<List<AddressResponse>> getAllAddresses() {
        Long userId = currentUserService.getCurrentUserId();
        return ResponseEntity.ok(getAllUserAddressesUseCase.execute(userId));
    }

    @Operation(summary = "Update an address", description = "Updates an existing delivery address for the currently authenticated user.")
    @PutMapping("/{addressId}")
    public ResponseEntity<AddressResponse> updateAddress(
            @PathVariable Long addressId,
            @RequestBody UpdateAddressRequest request) {
        Long userId = currentUserService.getCurrentUserId();
        return ResponseEntity
                .ok(updateUserAddressUseCase.execute(new UpdateAddressCommand(userId, addressId, request)));
    }

    @Operation(summary = "Delete an address", description = "Deletes a specific delivery address for the currently authenticated user.")
    @DeleteMapping("/{addressId}")
    public ResponseEntity<Void> deleteAddress(@PathVariable Long addressId) {
        Long userId = currentUserService.getCurrentUserId();
        deleteUserAddressUseCase.execute(new DeleteAddressCommand(userId, addressId));
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}

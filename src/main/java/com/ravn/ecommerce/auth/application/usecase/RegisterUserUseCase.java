package com.ravn.ecommerce.auth.application.usecase;

import com.ravn.ecommerce.auth.application.command.RegisterUserCommand;
import com.ravn.ecommerce.auth.application.dto.UserDTO;
import com.ravn.ecommerce.auth.application.exception.UserAlreadyExistsException;
import com.ravn.ecommerce.auth.domain.model.*;
import com.ravn.ecommerce.auth.domain.repository.UserRepository;
import com.ravn.ecommerce.auth.domain.service.PasswordHasher;
import com.ravn.ecommerce.auth.domain.service.PasswordPolicy;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
//@Service
@RequiredArgsConstructor
public class RegisterUserUseCase {
    private final UserRepository userRepository;
    private final PasswordHasher passwordHasher;
    private final PasswordPolicy passwordPolicy;

    public UserDTO execute(RegisterUserCommand registerUserCommand){
        Email email = new Email(registerUserCommand.email());
        if(userRepository.findByEmail(email).isPresent()){
            throw new UserAlreadyExistsException();
        }
        PasswordHash passwordHash = passwordHasher.hash(registerUserCommand.password());
        passwordPolicy.validate(passwordHash);

        User user = new User(
                UserId.generate(),
                email,
                passwordHash,
                UserRole.CLIENT,
                UserStatus.ACTIVE,
                Instant.now()
        );

        userRepository.save(user);
        return UserDTO.from(user);
    }
}

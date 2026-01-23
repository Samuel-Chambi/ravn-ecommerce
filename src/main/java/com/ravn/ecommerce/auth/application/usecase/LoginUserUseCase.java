package com.ravn.ecommerce.auth.application.usecase;

import com.ravn.ecommerce.auth.application.command.LoginCommand;
import com.ravn.ecommerce.auth.application.dto.AuthResultDTO;
import com.ravn.ecommerce.auth.domain.exception.InvalidCredentialsException;
import com.ravn.ecommerce.auth.domain.model.Email;
import com.ravn.ecommerce.auth.domain.model.User;
import com.ravn.ecommerce.auth.domain.repository.UserRepository;
import com.ravn.ecommerce.auth.domain.service.PasswordHasher;
import com.ravn.ecommerce.auth.infrastructure.security.AuthTokenGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
//@Service
public class LoginUserUseCase {
    private final UserRepository userRepository;
    private final PasswordHasher passwordHasher;
    private final AuthTokenGenerator tokenGenerator;
    public AuthResultDTO execute(LoginCommand loginCommand){
        User user = userRepository
                .findByEmail(new Email(loginCommand.email()))
                .orElseThrow(InvalidCredentialsException::new);
        user.ensureActiveUser();
        if(!passwordHasher.matches(loginCommand.password() , user.getPassword())){
            throw new InvalidCredentialsException();
        }
        String token = tokenGenerator.generate(user);
        return AuthResultDTO.from(user , token);
    }
}

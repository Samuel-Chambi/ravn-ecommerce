package com.ravn.ecommerce.application.services;

import com.ravn.ecommerce.domain.exceptions.UnauthorizedException;
import com.ravn.ecommerce.domain.model.user.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

/**
 * Service to retrieve the currently authenticated user.
 * <p>
 * TEMPORARY IMPLEMENTATION: Returns null until JWT authentication is
 * implemented.
 * <p>
 * Once JWT is implemented, this will extract the user from the SecurityContext.
 */
@Service
@Slf4j
public class CurrentUserService {

    /**
     * Gets the currently authenticated user from the security context.
     *
     * @return the authenticated User
     * @throws UnauthorizedException if no user is authenticated
     *                               <p>
     *                                                             TODO: Implement once JWT authentication is
     *                                                             ready
     */
    public User getCurrentUser() {
        // TODO: This method requires fetching the entity from DB or having a custom
        // UserDetails that holds the entity.
        // For now, we only need the ID for the use cases.
        throw new UnsupportedOperationException("Use getCurrentUserId() instead for now");
    }

    /**
     * Gets the ID of the currently authenticated user.
     *
     * @return the user ID
     * @throws UnauthorizedException if no user is authenticated
     */
    public Long getCurrentUserId() {
        return 2L;
        //        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
//
//        if (authentication == null || !authentication.isAuthenticated()
//                || "anonymousUser".equals(authentication.getPrincipal())) {
//            throw new UnauthorizedException("No authenticated user found");
//        }
//
//        Object principal = authentication.getPrincipal();
//        if (principal instanceof UserDetails) {
//            return Long.parseLong(((UserDetails) principal).getUsername());
//        } else if (principal instanceof String) {
//            // Fallback if principal is just the username string
//            try {
//                return Long.parseLong((String) principal);
//            } catch (NumberFormatException e) {
//                throw new UnauthorizedException("Invalid user ID in principal");
//            }
//        }
//
//        throw new UnauthorizedException("Could not extract user ID from authentication");
    }

    /**
     * Gets the ID of the currently authenticated user.
     *
     * @return the user ID
     * @throws UnauthorizedException if no user is authenticated
     */
//    public Long getCurrentUserId() {
//        return getCurrentUser().getId();
//    }

    /**
     * Checks if there is a currently authenticated user.
     *
     * @return true if a user is authenticated, false otherwise
     */
    public boolean isAuthenticated() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null && authentication.isAuthenticated();
    }
}

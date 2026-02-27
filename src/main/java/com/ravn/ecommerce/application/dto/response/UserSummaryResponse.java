package com.ravn.ecommerce.application.dto.response;

import com.ravn.ecommerce.domain.model.user.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserSummaryResponse {
    private Long id;
    private String email;

    public static UserSummaryResponse fromDomain(User user) {
        return UserSummaryResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .build();
    }
}

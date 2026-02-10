package com.ravn.ecommerce.domain.model.user;

import java.time.LocalDateTime;
import java.util.Objects;

public class PasswordReset {
    private Long id;
    private Long userId;
    private String token;
    private LocalDateTime expiresAt;
    private boolean used;

    public PasswordReset(Long id, boolean used, LocalDateTime expiresAt, String token, Long userId) {
        this.id = id;
        this.used = used;
        this.expiresAt = expiresAt;
        this.token = token;
        this.userId = userId;
    }

    protected PasswordReset() {
    }


    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }

    public boolean isValid() {
        return !used && !isExpired();
    }

    public void markAsUsed() {
        this.used = true;
    }


    public Long getId() {
        return id;
    }

    public Long getUserId() {
        return userId;
    }

    public String getToken() {
        return token;
    }

    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }

    public boolean isUsed() {
        return used;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PasswordReset that = (PasswordReset) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}

package com.ravn.ecommerce.domain.model.user;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class User {
    private Long id;
    private String email;
    private String passwordHash;
    private UserRole role;
    private boolean active;
    private LocalDateTime createdAt;
    private List<Address> addresses;

    public User(Long id, String email, String passwordHash, UserRole role, boolean active, LocalDateTime createdAt) {
        this.id = id;
        this.email = email;
        this.passwordHash = passwordHash;
        this.role = role;
        this.active = active;
        this.createdAt = createdAt;
        this.addresses = new ArrayList<>();
    }
    public void activate(){
        this.active = true;
    }
    public void deactivate() {
        this.active = false;
    }

    public boolean isManager(){
        return role == UserRole.MANAGER;
    }
    public boolean isClient() {return role == UserRole.CLIENT; }

    public void addAddress(Address address) {
        if (this.addresses == null) {
            this.addresses = new ArrayList<>();
        }

        if (this.addresses.isEmpty()) {
            address.setAsDefault();
        }

        this.addresses.add(address);
    }

    public void setDefaultAddress(Long addressId) {
        if (this.addresses == null) return;

        this.addresses.forEach(addr -> {
            if (addr.getId().equals(addressId)) {
                addr.setAsDefault();
            } else {
                addr.unsetDefault();
            }
        });
    }

    public Address getDefaultAddress() {
        if (this.addresses == null) return null;

        return this.addresses.stream()
                .filter(Address::isDefault)
                .findFirst()
                .orElse(null);
    }

    public List<Address> getAddresses() {
        return addresses;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public boolean isActive() {
        return active;
    }

    public UserRole getRole() {
        return role;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public String getEmail() {
        return email;
    }

    public Long getId() {
        return id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return Objects.equals(id, user.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}

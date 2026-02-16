package com.ravn.ecommerce.domain.model.user;

import java.time.LocalDateTime;
import java.util.Objects;

public class Address {
    private Long id;
    private Long userId;
    private String fullName;
    private String phone;
    private String street;
    private String city;
    private String state;
    private String country;
    private String zipCode;
    private boolean isDefault;
    private LocalDateTime createdAt;

    public Address(Long id, Long userId, String fullName, String phone, String street, String city, String state, String country, String zipCode, boolean isDefault, LocalDateTime createdAt) {
        this.id = id;
        this.userId = userId;
        this.fullName = fullName;
        this.phone = phone;
        this.street = street;
        this.city = city;
        this.state = state;
        this.country = country;
        this.zipCode = zipCode;
        this.isDefault = isDefault;
        this.createdAt = createdAt;
    }

    protected Address() {
    }

    public void setAsDefault() {
        this.isDefault = true;
    }

    public void unsetDefault() {
        this.isDefault = false;
    }

    public boolean isComplete() {
        return fullName != null && !fullName.isEmpty() &&
                street != null && !street.isEmpty() &&
                city != null && !city.isEmpty() &&
                country != null && !country.isEmpty() &&
                zipCode != null && !zipCode.isEmpty();
    }

    // Getters
    public Long getId() {
        return id;
    }

    public Long getUserId() {
        return userId;
    }

    public String getFullName() {
        return fullName;
    }

    public String getPhone() {
        return phone;
    }

    public String getStreet() {
        return street;
    }

    public String getCity() {
        return city;
    }

    public String getState() {
        return state;
    }

    public String getCountry() {
        return country;
    }

    public String getZipCode() {
        return zipCode;
    }

    public boolean isDefault() {
        return isDefault;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Address address = (Address) o;
        return Objects.equals(id, address.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

}

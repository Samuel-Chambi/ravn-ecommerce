package com.ravn.ecommerce.domain.model.user;

import lombok.*;

import java.time.LocalDateTime;
import java.util.Objects;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
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
    private LocalDateTime updatedAt;
    private LocalDateTime deletedAt;

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

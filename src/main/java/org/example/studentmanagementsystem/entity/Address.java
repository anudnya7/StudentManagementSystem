package org.example.studentmanagementsystem.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;

/**
 * A value object, not an entity: no id, no table of its own, no independent life.
 * @Embeddable fields become plain columns of the owning entity's table (see Student.address).
 */
@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Address {

    @Column(length = 100)
    private String street;

    @Column(length = 50)
    private String city;

    @Column(length = 50)
    private String state;

    @Column(length = 10)
    private String pincode;

    @Column(length = 50)
    private String country;
}
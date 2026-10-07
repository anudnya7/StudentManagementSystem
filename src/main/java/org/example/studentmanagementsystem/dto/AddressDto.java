package org.example.studentmanagementsystem.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;

import java.io.Serializable;

@Schema(description = "Optional postal address")
public record AddressDto(

        @Schema(example = "12 MG Road")
        @Size(max = 100, message = "Street must be up to 100 characters")
        String street,

        @Schema(example = "Pune")
        @Size(max = 50, message = "City must be up to 50 characters")
        String city,

        @Schema(example = "Maharashtra")
        @Size(max = 50, message = "State must be up to 50 characters")
        String state,

        @Schema(example = "411001")
        @Size(max = 10, message = "Pincode must be up to 10 characters")
        String pincode,

        @Schema(example = "India")
        @Size(max = 50, message = "Country must be up to 50 characters")
        String country
) implements Serializable {
}
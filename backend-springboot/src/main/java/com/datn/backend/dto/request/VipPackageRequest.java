package com.datn.backend.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class VipPackageRequest {
    @NotBlank(message = "Name is required")
    private String name;

    @NotNull(message = "Duration is required")
    @Min(value = 1, message = "Duration must be at least 1 month")
    private Integer durationMonth;

    @NotNull(message = "Price is required")
    private BigDecimal price;

    @NotBlank(message = "Currency is required")
    private String currency;

    private Boolean isActive;
}

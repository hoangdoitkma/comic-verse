package com.datn.backend.dto.response.dashboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TopComicsDto {
    private Integer id;
    private String name;
    private String thumbnail;
    private long views;
    private BigDecimal revenue; // Optional if we don't have per comic revenue
}

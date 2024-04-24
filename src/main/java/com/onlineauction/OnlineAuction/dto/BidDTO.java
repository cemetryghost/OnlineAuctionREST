package com.onlineauction.OnlineAuction.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BidDTO {
    private Long id;
    @NotNull
    private Long lotId;
    @NotNull
    private Long buyerId;
    @NotNull
    private BigDecimal bidAmount;

    private LotDTO lotDTO;
}

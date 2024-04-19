package com.onlineauction.OnlineAuction.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class BidDTO {
    private Long id;
    @NotNull
    private Long lotId;
    @NotNull
    private Long buyerId;
    @NotNull
    private BigDecimal bidAmount;
}

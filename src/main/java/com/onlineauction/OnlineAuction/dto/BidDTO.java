package com.onlineauction.OnlineAuction.dto;

import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
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

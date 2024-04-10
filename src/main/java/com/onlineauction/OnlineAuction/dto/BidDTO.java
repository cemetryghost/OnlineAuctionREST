package com.onlineauction.OnlineAuction.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class BidDTO {
    private Long id;
    private Long lotId;
    private Long buyerId;
    private BigDecimal bidAmount;
}

package com.onlineauction.OnlineAuction.dto;

import com.onlineauction.OnlineAuction.enums.StatusLot;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Getter
@Setter
@Valid
@NoArgsConstructor
@AllArgsConstructor
public class LotDTO {
    private Long id;
    @NotBlank(message = "Name cannot be empty")
    private String nameLots;
    @NotBlank(message = "Description cannot be empty")
    private String descriptionLots;
    @NotNull(message = "Start price cannot be empty")
    @DecimalMin(value = "0.0", inclusive = false, message = "Start price must be greater than 0")
    private BigDecimal startPrice;
    private BigDecimal currentPrice;
    @NotNull(message = "Step price cannot be empty")
    @DecimalMin(value = "0.0", inclusive = false, message = "Step price must be greater than 0")
    private BigDecimal stepPrice;
    @NotNull(message = "Date publication cannot be empty")
    private LocalDate publicationDate;
    @NotNull(message = "Date closing cannot be empty")
    private LocalDate closingDate;
    @NotBlank(message = "Condition cannot be empty")
    private String conditionLots;
    @NotNull(message = "Status cannot be empty")
    private StatusLot statusLots;
    @NotNull(message = "Category id cannot be empty")
    private Long categoryId;
    @NotNull(message = "Seller id cannot be empty")
    private Long sellerId;
    private Long currentBuyerId;

    private boolean hasImage;
}

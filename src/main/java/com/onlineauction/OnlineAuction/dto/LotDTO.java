package com.onlineauction.OnlineAuction.dto;

import com.onlineauction.OnlineAuction.enums.StatusLot;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
    @NotBlank(message = "Имя лота не может быть пустым")
    private String nameLots;
    @NotBlank(message = "Описание лота не может юыть пустым")
    private String descriptionLots;
    @NotNull(message = "Начальная лота не может быть пустой")
    @DecimalMin(value = "0.0", inclusive = false, message = "Начальная цена должна быть больше 0")
    private BigDecimal startPrice;
    private BigDecimal currentPrice;
    @NotNull(message = "Шаг цены не может быть пустым")
    @DecimalMin(value = "0.0", inclusive = false, message = "Шаг цены должен быть больше 0")
    private BigDecimal stepPrice;
    @NotNull(message = "Дата публикации не может быть пустой")
    private LocalDate publicationDate;
    @NotNull(message = "Дата закрытия не может быть пустой")
    private LocalDate closingDate;
    @NotBlank(message = "Состояние лота не может быть пустым")
    private String conditionLots;
    @NotNull(message = "Статус лота не может быть пустым")
    private StatusLot statusLots;
    @NotNull(message = "Id категории не может быть пустым")
    private Long categoryId;
    @NotNull(message = "Id продавца не может быть пустым")
    private Long sellerId;
    private Long currentBuyerId;
    private boolean hasImage;
}

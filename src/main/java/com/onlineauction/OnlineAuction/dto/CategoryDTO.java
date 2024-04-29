package com.onlineauction.OnlineAuction.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class CategoryDTO {
    private Long id;
    @NotBlank (message = "Имя категории не может быть пустым")
    private String nameCategory;

    private LotDTO lotDTO;
}

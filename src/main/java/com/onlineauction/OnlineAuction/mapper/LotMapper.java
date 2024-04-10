package com.onlineauction.OnlineAuction.mapper;

import com.onlineauction.OnlineAuction.context.MappingContext;
import com.onlineauction.OnlineAuction.dto.LotDTO;
import com.onlineauction.OnlineAuction.entity.Lot;
import com.onlineauction.OnlineAuction.entity.UserAccounts;
import org.mapstruct.*;

@Mapper(componentModel = "spring", uses = {MappingContext.class})
public interface LotMapper {

    @Mapping(target = "categoryId", source = "categoryId.id")
    @Mapping(target = "sellerId", source = "sellerId.id")
    @Mapping(target = "currentBuyerId", source = "currentBuyerId.id")
    LotDTO lotToLotDTO(Lot lot);

    @Mapping(target = "categoryId.id", source = "categoryId")
    @Mapping(target = "sellerId.id", source = "sellerId")
    @Mapping(target = "currentBuyerId", source = "currentBuyerId", qualifiedByName = "idToUserAccount")
    Lot lotDTOToLot(LotDTO lotDTO, @Context MappingContext context);

    @Named("idToUserAccount")
    default UserAccounts idToUserAccount(Long id, @Context MappingContext context) {
        if (id == null) {
            return null;
        }
        return context.getUserRepository().findById(id).orElse(null);
    }
    @AfterMapping
    default void setHasImage(Lot lot, @MappingTarget LotDTO lotDTO) {
        lotDTO.setHasImage(lot.getImage() != null && lot.getImage().length > 0);
    }
}
package com.onlineauction.OnlineAuction.mapper;

import com.onlineauction.OnlineAuction.dto.LotDTO;
import com.onlineauction.OnlineAuction.entity.Lot;
import com.onlineauction.OnlineAuction.entity.UserAccounts;
import com.onlineauction.OnlineAuction.mapper.context.MappingContext;
import org.mapstruct.*;


@Mapper(componentModel = "spring", uses = {UserMapper.class, MappingContext.class})
public interface LotMapper {

    @Mapping(target = "categoryId", source = "categoryId.id")
    @Mapping(target = "sellerId", source = "sellerId.id")
    @Mapping(target = "currentBuyerId", source = "currentBuyerId.id")
    @Mapping(target = "sellerDetails", source = "sellerId")
    @Mapping(target = "buyerDetails", source = "currentBuyerId")
    @Mapping(target = "hasImage", expression = "java(lot.getImage() != null && lot.getImage().length > 0)")
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
}


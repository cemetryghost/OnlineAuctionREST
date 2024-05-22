package com.onlineauction.OnlineAuction.mapper;

import com.onlineauction.OnlineAuction.dto.BidDTO;
import com.onlineauction.OnlineAuction.entity.Bid;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface BidMapper {

    @Mapping(target = "lotId", source = "lot.id")
    @Mapping(target = "buyerId", source = "buyer.id")
    BidDTO bidToBidDTO(Bid bid);

    @Mapping(target = "lot", ignore = true)
    @Mapping(target = "buyer", ignore = true)
    Bid BidDTOtoBid(BidDTO bidDTO);
}


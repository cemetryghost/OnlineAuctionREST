package com.onlineauction.OnlineAuction.service;

import com.onlineauction.OnlineAuction.dto.BidDTO;

import java.math.BigDecimal;
import java.util.List;

public interface BidService {
    BidDTO placeBid(BidDTO bidDTO);
    List<BidDTO> getAllBids();
    BidDTO getBidById(Long id);
    BidDTO updateBid(Long id, BigDecimal newBidAmount);
    void deleteBid(Long id);
    List<BidDTO> getBidsByLotId(Long lotId);
    List<BidDTO> getMyBidsWithLotDetails();

}


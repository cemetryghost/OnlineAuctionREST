package com.onlineauction.OnlineAuction.repository;

import com.onlineauction.OnlineAuction.entity.Bid;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BidRepository extends JpaRepository<Bid, Long> {
    List<Bid> findByLotId(Long lotId);
    List<Bid> findByBuyerId(Long buyerId);
}

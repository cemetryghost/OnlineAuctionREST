package com.onlineauction.OnlineAuction.repository;

import com.onlineauction.OnlineAuction.entity.Bid;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BidRepository extends JpaRepository<Bid, Long> {
}

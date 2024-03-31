package com.onlineauction.OnlineAuction.repository;

import com.onlineauction.OnlineAuction.entity.Lot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LotRepository extends JpaRepository<Lot, Long> {
}

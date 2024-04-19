package com.onlineauction.OnlineAuction.repository;

import com.onlineauction.OnlineAuction.entity.Lot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LotRepository extends JpaRepository<Lot, Long> {
    List<Lot> findByCategoryIdId(Long categoryId);
    List<Lot> findBySellerIdId(Long sellerId);
    List<Lot> findBySellerId_Id(Long sellerId);

}

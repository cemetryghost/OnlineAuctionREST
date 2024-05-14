package com.onlineauction.OnlineAuction.repository;

import com.onlineauction.OnlineAuction.entity.Lot;
import com.onlineauction.OnlineAuction.enums.StatusLot;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface LotRepository extends JpaRepository<Lot, Long> {
    List<Lot> findByCategoryIdId(Long categoryId);
    List<Lot> findBySellerIdId(Long sellerId);
    List<Lot> findBySellerId_Id(Long sellerId);
    List<Lot> findBySellerId_IdAndStatusLots(Long sellerId, StatusLot statusLots);
    List<Lot> findByStatusLots(StatusLot status);
    List<Lot> findByCategoryIdIdAndStatusLots(Long categoryId, StatusLot status);
    List<Lot> findAllByClosingDateBeforeAndStatusLotsNot(LocalDate now, StatusLot statusLot);
    List<Lot> findAll(Specification<Lot> spec);
    List<Lot> findByCurrentBuyerId_Id(Long currentBuyerId);
}

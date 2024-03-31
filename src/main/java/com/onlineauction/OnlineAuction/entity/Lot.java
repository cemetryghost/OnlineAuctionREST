package com.onlineauction.OnlineAuction.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Lot {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idlots;
    @Column(nullable = false)
    private String nameLots;
    @Column(nullable = false)
    private String descriptionLots;
    @Column(nullable = false)
    private BigDecimal startPrice;
    private BigDecimal currentPrice;
    @Column(nullable = false)
    private BigDecimal stepPrice;
    @Column(nullable = false)
    private LocalDate publicationDate;
    @Column(nullable = false)
    private LocalDate closingDate;
    @Column(nullable = false)
    private String conditionLots;
    @Column(nullable = false)
    private String statusLots;

    @ManyToOne
    @JoinColumn(name = "category_id")
    private Category category;

    @ManyToOne
    @JoinColumn(name = "seller_id")
    private UserAccounts seller;

    @ManyToOne
    @JoinColumn(name = "current_buyer_id")
    private UserAccounts currentBuyer;
}


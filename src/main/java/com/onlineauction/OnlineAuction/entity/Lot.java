package com.onlineauction.OnlineAuction.entity;

import com.onlineauction.OnlineAuction.enums.StatusLot;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.sql.Blob;
import java.time.LocalDate;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Lot {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_lots")
    private Long id;
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

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusLot statusLots;

    @Lob
    private byte[] image;

    @ManyToOne
    @JoinColumn(name = "category_id", nullable = false)
    private Category categoryId;

    @ManyToOne
    @JoinColumn(name = "seller_id", nullable = false)
    private UserAccounts sellerId;

    @ManyToOne
    @JoinColumn(name = "current_buyer_id")
    private UserAccounts currentBuyerId;

}


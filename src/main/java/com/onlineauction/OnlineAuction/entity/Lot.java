package com.onlineauction.OnlineAuction.entity;

import com.onlineauction.OnlineAuction.enums.StatusLot;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "lot", indexes = {
        @Index(name = "idx_lot_name_lots", columnList = "name_lots")
})
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
    @OnDelete(action = OnDeleteAction.CASCADE)
    private UserAccounts sellerId;

    @ManyToOne
    @JoinColumn(name = "current_buyer_id")
    @OnDelete(action = OnDeleteAction.NO_ACTION)
    private UserAccounts currentBuyerId;
}


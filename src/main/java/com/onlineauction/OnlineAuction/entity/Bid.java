package com.onlineauction.OnlineAuction.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.math.BigDecimal;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Bid {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_bids")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "lot_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE) // Каскадное удаление
    private Lot lot;

    @ManyToOne
    @JoinColumn(name = "buyer_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE) // Каскадное удаление
    private UserAccounts buyer;


    @Column(nullable = false)
    private BigDecimal bidAmount;
}


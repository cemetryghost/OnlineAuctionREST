package com.onlineauction.OnlineAuction.specification;

import com.onlineauction.OnlineAuction.entity.Lot;
import com.onlineauction.OnlineAuction.enums.StatusLot;
import org.springframework.data.jpa.domain.Specification;

public class LotSpecification {

    public static Specification<Lot> hasKeyword(String keyword) {
        return (root, query, criteriaBuilder) -> {
            if (keyword == null || keyword.isEmpty()) {
                return criteriaBuilder.isTrue(criteriaBuilder.literal(true)); 
            }
            return criteriaBuilder.like(criteriaBuilder.lower(root.get("nameLots")), "%" + keyword.toLowerCase() + "%");
        };
    }

    public static Specification<Lot> isActive() {
        return (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("statusLots"), StatusLot.ACTIVE_LOT);
    }

    public static Specification<Lot> hasCategory(Long categoryId) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("categoryId").get("id"), categoryId);
    }
}


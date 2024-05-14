package com.onlineauction.OnlineAuction.repository;

import com.onlineauction.OnlineAuction.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
    boolean existsByNameCategory(String nameCategory);
    @Query("SELECT COUNT(l) > 0 FROM Lot l WHERE l.categoryId = :category")
    boolean existsLotsByCategoryId(@Param("category") Category category);
}


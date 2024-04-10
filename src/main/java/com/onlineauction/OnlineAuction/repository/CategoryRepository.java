package com.onlineauction.OnlineAuction.repository;

import com.onlineauction.OnlineAuction.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
    boolean existsByNameCategory(String nameCategory);
}

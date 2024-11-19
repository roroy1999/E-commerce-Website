package com.monk.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.monk.model.Product;

@Repository
public interface ProductRepository extends JpaRepository<Product, Integer> {
    // You can add custom queries here if needed
}

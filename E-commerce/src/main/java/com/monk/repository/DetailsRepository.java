package com.monk.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.monk.model.Details;

@Repository
public interface DetailsRepository extends JpaRepository<Details, Integer> {
	
	public Optional<Details> findByProductId(int id);
	public Optional<List<Details>> findByType(String value);
}

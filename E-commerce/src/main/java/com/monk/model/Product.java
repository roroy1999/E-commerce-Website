package com.monk.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class Product {

	@Id
	private int productId;
	private int quantity;
	private int price;
	private boolean free;
	private int discount;
	@JsonIgnore
	@ToString.Exclude
	@ManyToOne
	Details details;
}

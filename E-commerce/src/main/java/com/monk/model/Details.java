package com.monk.model;

import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.SequenceGenerator;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class Details {

	@Id
	@SequenceGenerator(name = "couponId", sequenceName = "couponId")
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "couponId")
	private int couponId;
	private String type;
	private int threshold;
	private int discount;
	private int productId;
	@OneToMany(mappedBy = "details", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
	private List<Product> product;
	private int repition_limit;
}

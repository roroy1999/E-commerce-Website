package com.monk.dto;

import java.util.List;
import java.util.Map;

import lombok.Data;

@Data
public class AfterCouponApplied {
	List<Map<String, Object>> items;
	int total_price;
	int total_discount;
	int final_price;
}

package com.monk.controller;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.springframework.web.bind.annotation.RestController;

import com.monk.dto.ApplicableCoupons;
import com.monk.dto.UpdatedCartDTO;
import com.monk.model.Details;
import com.monk.service.CouponService;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;


@RestController
//@RequestMapping()
public class CouponController {
	
	private Logger logger;
	private CouponService couponService;
	
	public CouponController(Logger logger,CouponService couponService) {
		this.logger = logger;
		this.couponService = couponService;
	}

	
	@PostMapping("/coupons")
	public String addCoupons(@RequestBody Map<String, Object> entity) {
		//TODO: process POST request
		//System.out.println(entity);
		logger.info("Recieved Coupon is :{}",entity);
		
		return couponService.savaCoupon(entity);
	}
	
	@PostMapping("/applicable-coupons")
	public ApplicableCoupons applicableCoupons(@RequestBody Map<String, Object> entity) {
		//TODO: process POST request
		logger.info("Recieved Items are :{}",entity);
		
		return couponService.applicableCoupons(entity);
	}
	
	@PostMapping(path="/apply-coupon/{id}")
	public UpdatedCartDTO applyCouponById(@RequestBody Map<String, Object> entity, @PathVariable("id") int id) {
		//TODO: process POST request
		logger.info("Recieved cart items are :{} and applied coupon is {}",entity,id);
		
		
		return couponService.applyCouponsById(entity,id);
	}
	
	@GetMapping("/coupons")
	public List<Details> getAllCoupon() {
		return couponService.getAllCoupons();
	}
	
}

package com.monk.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.monk.dto.ApplicableCoupons;
import com.monk.model.Details;
import com.monk.service.CouponService;

import org.springframework.web.bind.annotation.GetMapping;
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
		//System.out.println(entity);
		logger.info("Recieved Coupon is :{}",entity);
		
		return couponService.applicableCoupons(entity);
		
		//return couponService.savaCoupon(entity);
	}
	
	@GetMapping("/coupons")
	public List<Details> getAllCoupon() {
		return couponService.getAllCoupons();
	}
	
}

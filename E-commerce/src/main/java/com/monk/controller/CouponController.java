package com.monk.controller;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.springframework.web.bind.annotation.RestController;

import com.monk.dto.ApplicableCoupons;
import com.monk.dto.UpdatedCartDTO;
import com.monk.model.Details;
import com.monk.service.CouponService;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

@RestController
// @RequestMapping()
public class CouponController {

	private Logger logger;
	private CouponService couponService;

	public CouponController(Logger logger, CouponService couponService) {
		this.logger = logger;
		this.couponService = couponService;
	}

	@PostMapping("/coupons")
	public String addCoupons(@RequestBody Map<String, Object> entity) {
		logger.info("Recieved Coupon is :{}", entity);

		return couponService.savaCoupon(entity);
	}

	@PostMapping("/applicable-coupons")
	public ApplicableCoupons applicableCoupons(@RequestBody Map<String, Object> entity) {
		logger.info("Recieved Items are :{}", entity);

		return couponService.applicableCoupons(entity);
	}

	@PostMapping(path = "/apply-coupon/{id}")
	public UpdatedCartDTO applyCouponById(@RequestBody Map<String, Object> entity, @PathVariable("id") int id) {
		logger.info("Recieved cart items are :{} and applied coupon is {}", entity, id);

		return couponService.applyCouponsById(entity, id);
	}

	// ● GET /coupons: Retrieve all coupons.
	@GetMapping("/coupons")
	public List<Map<String, Object>> getAllCoupon() {
		return couponService.getAllCoupons();
	}

	// ● GET /coupons/{id}: Retrieve a specific coupon by its ID.
	@GetMapping("/coupons/{id}")
	public Map<String, Object> getCouponById(@PathVariable("id") int id) {
		return couponService.getCouponById(id);
	}

	// ● PUT /coupons/{id}: Update a specific coupon by its ID.
	@PutMapping("/coupons/{id}")
	public String updateCouponById(@PathVariable("id") int id, @RequestBody Map<String, Object> entity) {
		return couponService.updateCouponById(id, entity);
	}

	// ● DELETE /coupons/{id}: Delete a specific coupon by its ID.
	@DeleteMapping("/coupons/{id}")
	public String deleteCouponById(@PathVariable("id") int id) {
		return couponService.deleteCouponById(id);
	}

}

package com.monk.controller;

import java.util.Map;

import org.slf4j.Logger;
import org.springframework.web.bind.annotation.RestController;

import com.monk.service.CouponService;

import org.springframework.http.ResponseEntity;
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

	// ● POST /coupons: Create a new coupon.
	@PostMapping("/coupons")
	public ResponseEntity<?> addCoupons(@RequestBody Map<String, Object> entity) {
		logger.info("Recieved Coupon is :{}", entity);
		return couponService.savaCoupon(entity);
	}

	// ● POST /applicable-coupons: Fetch all applicable coupons for a given cart and
	// calculate the total discount that will be applied by each coupon.
	@PostMapping("/applicable-coupons")
	public ResponseEntity<?> applicableCoupons(@RequestBody Map<String, Object> entity) {
		logger.info("Recieved Items are :{}", entity);
		return couponService.applicableCoupons(entity);
	}

	// ● POST /apply-coupon/{id}: Apply a specific coupon to the cart and return the
	// updated cart with discounted prices for each item.
	@PostMapping(path = "/apply-coupon/{id}")
	public ResponseEntity<?> applyCouponById(@RequestBody Map<String, Object> entity, @PathVariable int id) {
		logger.info("Recieved cart items are :{} and applied coupon is {}", entity, id);

		return couponService.applyCouponsById(entity, id);
	}

	// ● GET /coupons: Retrieve all coupons.
	@GetMapping("/coupons")
	public ResponseEntity<?> getAllCoupon() {
		return couponService.getAllCoupons();
	}

	// ● GET /coupons/{id}: Retrieve a specific coupon by its ID.
	@GetMapping("/coupons/{id}")
	public ResponseEntity<?> getCouponById(@PathVariable int id) {
		return couponService.getCouponById(id);
	}

	// ● PUT /coupons/{id}: Update a specific coupon by its ID.
	@PutMapping("/coupons/{id}")
	public ResponseEntity<?> updateCouponById(@PathVariable int id, @RequestBody Map<String, Object> entity) {
		return couponService.updateCouponById(id, entity);
	}

	// ● DELETE /coupons/{id}: Delete a specific coupon by its ID.
	@DeleteMapping("/coupons/{id}")
	public ResponseEntity<?> deleteCouponById(@PathVariable int id) {
		return couponService.deleteCouponById(id);
	}

	// ● GET /products: Get all Products
	@GetMapping("/products")
	public ResponseEntity<?> getProducts() {
		return couponService.getProducts();
	}

	// ● DELETE /product/{id}: Delete Product based on Id
	@DeleteMapping("/product/{id}")
	public ResponseEntity<?> deleteProduct(@PathVariable int id) {
		return couponService.deleteProduct(id);
	}

}

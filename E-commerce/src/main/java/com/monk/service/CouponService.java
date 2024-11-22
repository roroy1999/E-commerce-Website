package com.monk.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.monk.dto.AfterCouponApplied;
import com.monk.dto.ApplicableCoupons;
import com.monk.dto.CouponsDTO;
import com.monk.dto.UpdatedCartDTO;
import com.monk.model.Details;
import com.monk.model.Product;
import com.monk.repository.DetailsRepository;
import com.monk.repository.ProductRepository;
import com.monk.utils.CouponConstants;

import jakarta.persistence.EntityNotFoundException;

@Service
public class CouponService {
	DetailsRepository detailsRepository;

	ProductRepository productRepository;

	@Autowired
	private Logger logger;

	public CouponService(DetailsRepository detailsRepository, ProductRepository productRepository) {
		this.detailsRepository = detailsRepository;
		this.productRepository = productRepository;
	}

	public ResponseEntity<?> getCouponById(int id) {
		// Need Handle here
		Details detail = null;
		try {
			detail = detailsRepository.findById(id).orElse(null);
			if (detail == null) {
				return new ResponseEntity<String>(CouponConstants.COUPON_NOT_FOUND, HttpStatus.BAD_REQUEST);
			}
		} catch (Exception e) {
			logger.error("Error in getCouponById(): {}", e);
		}

		return new ResponseEntity<Map<String, Object>>(getCouponBasedOnType(detail, detail.getType()), HttpStatus.OK);
	}

	public ResponseEntity<List<Map<String, Object>>> getAllCoupons() {
		List<Map<String, Object>> coupons = new ArrayList<>();
		try {
			List<Details> details = detailsRepository.findAll();
			for (Details detail : details) {
				String type = detail.getType();
				coupons.add(getCouponBasedOnType(detail, type));
			}
		} catch (Exception e) {
			logger.error("Error in getAllCoupons(): {}", e);
		}

		return new ResponseEntity<>(coupons, HttpStatus.OK);
	}

	private Map<String, Object> getCouponBasedOnType(Details detail, String type) {
		Map<String, Object> coupon = null;
		try {
			coupon = new HashMap<>();
			Map<String, Object> couponDetail = new HashMap<>();
			coupon.put(CouponConstants.TYPE, type);
			coupon.put(CouponConstants.COUPON_ID, detail.getCouponId());
			switch (type) {
				case CouponConstants.CART_WISE:
					couponDetail.put(CouponConstants.THRESHOLD, detail.getThreshold());
					couponDetail.put(CouponConstants.DISCOUNT, detail.getDiscount());
					break;
				case CouponConstants.PRODUCT_WISE:
					couponDetail.put(CouponConstants.PRODUCT_ID, detail.getProductId());
					couponDetail.put(CouponConstants.DISCOUNT, detail.getDiscount());
					break;
				case "bxgy":
					List<Map<String, Integer>> getProducts = new ArrayList<>();
					List<Map<String, Integer>> buyProducts = new ArrayList<>();
					for (Product product : detail.getProduct()) {
						Map<String, Integer> productDetail = Map.of(
								CouponConstants.PRODUCT_ID, product.getProductId(),
								CouponConstants.QUANTITY, product.getQuantity());
						if (product.isFree()) {
							getProducts.add(productDetail);
						} else {
							buyProducts.add(productDetail);
						}
					}
					couponDetail.put(CouponConstants.GET_PRODUCTS, getProducts);
					couponDetail.put(CouponConstants.BUY_PRODUCTS, buyProducts);
					couponDetail.put(CouponConstants.REPETITION_LIMIT, detail.getRepition_limit());
					break;
				default:
					throw new IllegalArgumentException(CouponConstants.UNKNOWN_COUPON_TYPE + type);
			}
			coupon.put("details", couponDetail);
		} catch (Exception e) {
			logger.error("Error in getCouponBasedOnType(): {}", e);
		}
		return coupon;
	}

	public ResponseEntity<?> savaCoupon(Map<String, Object> entity) {
		Details details = createDetails(entity);
		logger.info("Details is :{}", details);
		if (details == null) {
			return new ResponseEntity<String>(CouponConstants.DETAILS_IS_EMPTY, HttpStatus.BAD_REQUEST);
		}
		return saveBasedOnType(details);

	}

	public ResponseEntity<String> saveBasedOnType(Details details) {
		try {
			if (details.getType().equals(CouponConstants.NO_TYPE)
					|| (!details.getType().equals(CouponConstants.CART_WISE)
							&& !details.getType().equals(CouponConstants.PRODUCT_WISE)
							&& !details.getType().equals("bxgy"))) {
				return new ResponseEntity<String>(CouponConstants.INVALID_TYPE, HttpStatus.BAD_REQUEST);
			} else if (details.getType().equals(CouponConstants.PRODUCT_WISE)
					&& details.getProduct() != new ArrayList<Product>()) {
				int id = details.getProduct().get(0).getProductId();
				Product product = productRepository.findById(id).orElse(new Product());
				product.setProductId(id);
				product.setDiscount(details.getDiscount());
				productRepository.save(product);
				details.setProductId(id);
				Details detail_old = detailsRepository.findByProductId(id).orElse(details);
				detail_old.setProduct(new ArrayList<Product>());
				detailsRepository.save(detail_old);
				return new ResponseEntity<String>(CouponConstants.PRODUCT_DISCOUNT_ADDED, HttpStatus.CREATED);
			}

			detailsRepository.save(details);
		} catch (Exception e) {
			logger.error("Error in saveBasedOnType(): {}", e);
		}
		return new ResponseEntity<String>(CouponConstants.COUPON_ADDED, HttpStatus.CREATED);

	}

	@SuppressWarnings("unchecked")
	public Details createDetails(Map<String, Object> entity) {
		Details details = new Details();
		try {
			String type = (String) entity.getOrDefault(CouponConstants.TYPE, CouponConstants.NO_TYPE);
			Map<String, Object> detail = (Map<String, Object>) entity.getOrDefault("details", null);
			if (detail != null) {
				int discount = (int) detail.getOrDefault(CouponConstants.DISCOUNT, 0);
				int threshold = (int) detail.getOrDefault(CouponConstants.THRESHOLD, 0);
				int repition_limit = (int) detail.getOrDefault(CouponConstants.REPETITION_LIMIT, 0);
				List<Map<String, Object>> buyProducts = (List<Map<String, Object>>) detail.getOrDefault(
						CouponConstants.BUY_PRODUCTS,
						new ArrayList<>());
				List<Map<String, Object>> getProducts = (List<Map<String, Object>>) detail.getOrDefault(
						CouponConstants.GET_PRODUCTS,
						new ArrayList<>());
				List<Product> products = new ArrayList<>();
				details.setType(type);
				details.setRepition_limit(repition_limit);
				details.setThreshold(threshold);
				details.setDiscount(discount);
				for (Map<String, Object> item : getProducts) {
					products.add(setProduct(item, true, details));
				}
				for (Map<String, Object> item : buyProducts) {
					products.add(setProduct(item, false, details));
				}
				if ((int) detail.getOrDefault(CouponConstants.PRODUCT_ID, 0) != 0) {
					Product product = new Product();
					int productId = (int) detail.getOrDefault(CouponConstants.PRODUCT_ID, 0);
					product.setProductId(productId);
					product.setDiscount(discount);
					products.add(product);
				}
				details.setProduct(products);
				return details;
			}
		} catch (Exception e) {
			logger.error("Error in createDetails(): {}", e);
		}
		return null;
	}

	private Product setProduct(Map<String, Object> item, boolean isFree, Details details) {
		Product product = null;
		try {

			product = productRepository.findById((int) item.get(CouponConstants.PRODUCT_ID))
					.orElse(new Product());
			product.setProductId((int) item.getOrDefault(CouponConstants.PRODUCT_ID, 0));
			product.setDetails(details);
			product.setFree(isFree);
			product.setPrice((int) item.getOrDefault(CouponConstants.PRICE, 0));
			product.setQuantity((int) item.getOrDefault(CouponConstants.QUANTITY, 0));
		} catch (Exception e) {
			logger.error("Error in setProduct(): {}", e);
		}
		return product;
	}

	@SuppressWarnings("unchecked")
	public ResponseEntity<?> applicableCoupons(Map<String, Object> entity) {
		ApplicableCoupons applicableCoupons = null;
		try {
			applicableCoupons = new ApplicableCoupons();
			Map<String, Object> cart = (Map<String, Object>) entity.getOrDefault(CouponConstants.CART, null);
			List<Map<String, Object>> items = (List<Map<String, Object>>) cart.getOrDefault(CouponConstants.ITEMS,
					null);
			if (cart == null || items == null) {
				return new ResponseEntity<String>(CouponConstants.PLEASE_PROVIDE_VALID_CART, HttpStatus.BAD_REQUEST);
			}
			List<Product> products = new ArrayList<>();
			for (Map<String, Object> item : items) {
				Product product = new Product();
				product.setProductId((int) item.getOrDefault(CouponConstants.PRODUCT_ID, 0));
				product.setPrice((int) item.getOrDefault(CouponConstants.PRICE, 0));
				product.setQuantity((int) item.getOrDefault(CouponConstants.QUANTITY, 0));
				products.add(product);
			}

			List<CouponsDTO> couponsDTOs = new ArrayList<>();
			couponsDTOs.addAll(bxgyCoupon(products));
			couponsDTOs.add(cartWiseCoupon(products));
			couponsDTOs.addAll(productWiseCoupon(products));

			applicableCoupons.setApplicable_coupons(couponsDTOs);

		} catch (Exception e) {
			logger.error("Error in applicableCoupons(): {}", e);
		}
		return new ResponseEntity<ApplicableCoupons>(applicableCoupons, HttpStatus.OK);
	}

	public CouponsDTO cartWiseCoupon(List<Product> products) {
		CouponsDTO couponsDTO = null;
		try {
			couponsDTO = new CouponsDTO();
			List<Details> detail_old = detailsRepository.findByType(CouponConstants.CART_WISE)
					.orElse(new ArrayList<Details>());
			Details details = detail_old.get(0);
			double total = 0;
			double threshold = details.getThreshold();
			double discount = details.getDiscount();

			for (Product product : products) {
				double quantity = product.getQuantity();
				double price = product.getPrice();
				total += (quantity * price);
			}
			int part = 0;
			if (total > threshold) {
				part = (int) ((discount / 100) * total);
			}

			couponsDTO.setCoupon_id(details.getCouponId());
			couponsDTO.setDiscount(part);
			couponsDTO.setType(details.getType());
		} catch (Exception e) {
			logger.error("Error in cartWiseCoupon(): {}", e);
		}

		return couponsDTO;

	}

	public List<CouponsDTO> bxgyCoupon(List<Product> products) {
		List<CouponsDTO> couponsDTOs = new ArrayList<>();
		try {
			List<Details> details = detailsRepository.findByType(CouponConstants.BXGY).orElse(new ArrayList<Details>());
			Map<Integer, Details> uniqueDetailsMap = details.stream()
					.collect(Collectors.toMap(Details::getCouponId, detail -> detail,
							(existing, replacement) -> existing));

			List<Details> uniqueDetails = new ArrayList<>(uniqueDetailsMap.values());
			for (Product product : products) {

				Product product_original = productRepository.findById(product.getProductId()).orElse(new Product());
				if (!product_original.isFree()) {
					Details productDetails = product_original.getDetails();
					Details presentDetails = new Details();
					for (Details det : uniqueDetails) {
						if (det.getCouponId() == productDetails.getCouponId()) {
							presentDetails = det;
							break;
						}
					}
					int inCartProductQuantity = product.getQuantity();
					int acceptedProductQuntity = product_original.getQuantity();
					if (presentDetails.getRepition_limit() > 0 && inCartProductQuantity >= acceptedProductQuntity) {
						presentDetails.setRepition_limit(
								presentDetails.getRepition_limit() - (inCartProductQuantity / acceptedProductQuntity));
						List<Product> freeProducts = presentDetails.getProduct();
						Product freeProduct = freeProducts.stream().filter(x -> x.isFree()).findFirst().orElse(null);
						Product productCart = products.stream()
								.filter(x -> x.getProductId() == freeProduct.getProductId())
								.findFirst().orElse(null);
						int freePrice = productCart.getPrice();
						int discount = (inCartProductQuantity / acceptedProductQuntity) * freePrice;
						final int id = presentDetails.getCouponId();
						CouponsDTO couponsDTO_Old = couponsDTOs.stream().filter(x -> x.getCoupon_id() == id).findFirst()
								.orElse(null);
						if (couponsDTO_Old == null) {
							CouponsDTO couponsDTO = new CouponsDTO();
							couponsDTO.setType(CouponConstants.BXGY);
							couponsDTO.setDiscount(discount);
							couponsDTO.setCoupon_id(presentDetails.getCouponId());
							couponsDTOs.add(couponsDTO);
						} else {
							couponsDTO_Old.setDiscount(couponsDTO_Old.getDiscount() + discount);
						}
					}
				}
			}
		} catch (Exception e) {
			logger.error("Error in bxgyCoupon(): {}", e);
		}
		return couponsDTOs;
	}

	public List<CouponsDTO> productWiseCoupon(List<Product> products) {
		List<CouponsDTO> couponsDTOs = new ArrayList<>();
		try {
			for (Product productCart : products) {
				Details details = detailsRepository.findByProductId(productCart.getProductId()).orElse(new Details());
				Product product_original = productRepository.findById(productCart.getProductId()).orElse(new Product());
				double discount = product_original.getDiscount();
				if (discount > 0) {
					double total = 0.0;
					double quantity = productCart.getQuantity();
					double price = productCart.getPrice();

					total += (quantity * price);
					int discountAmount = (int) ((discount / 100) * total);
					CouponsDTO couponsDTO = new CouponsDTO();
					couponsDTO.setCoupon_id(details.getCouponId());
					couponsDTO.setDiscount(discountAmount);
					couponsDTO.setType(details.getType());
					couponsDTOs.add(couponsDTO);
				}

			}
		} catch (Exception e) {
			logger.error("Error in bxgyCoupon(): {}", e);
		}

		return couponsDTOs;
	}

	@SuppressWarnings("unchecked")
	public ResponseEntity<?> applyCouponsById(Map<String, Object> entity, int id) {
		UpdatedCartDTO updatedCartDTO = new UpdatedCartDTO();
		try {

			Map<String, Object> cart = (Map<String, Object>) entity.getOrDefault(CouponConstants.CART, null);
			List<Map<String, Object>> items = (List<Map<String, Object>>) cart.getOrDefault(CouponConstants.ITEMS,
					null);
			if (cart == null || items == null) {
				return new ResponseEntity<String>(CouponConstants.PLEASE_PROVIDE_VALID_CART, HttpStatus.BAD_REQUEST);
			}
			List<Map<String, Object>> productMapList = new ArrayList<>();
			List<Product> products = new ArrayList<>();

			for (Map<String, Object> item : items) {
				productMapList.add(mapProductsAndProductMap(item, products));
			}

			ApplicableCoupons applicableCoupons = (ApplicableCoupons) applicableCoupons(entity).getBody();
			List<CouponsDTO> coupons = applicableCoupons.getApplicable_coupons();

			CouponsDTO coupon = coupons.stream().filter(x -> x.getCoupon_id() == id).findFirst().orElse(null);
			double totalPrice = products.stream().reduce(0.0, (c, y) -> c + (y.getPrice() * y.getQuantity()),
					Double::sum);
			// logger.info("Total price : " + totalPrice);
			double discount = coupon.getDiscount();
			double discountPrice = totalPrice - discount;
			String type = coupon.getType();
			AfterCouponApplied afterCouponApplied = new AfterCouponApplied();
			Details details = detailsRepository.findById(coupon.getCoupon_id()).orElse(new Details());
			if (type.equals("cart-wise")) {
				// int size = details.getProduct().size();

				int totalProduct = products.stream().reduce(0, (c, y) -> c + (y.getQuantity()), Integer::sum);
				double offerPerProduct = discount / totalProduct;
				updatedCartDTO.setUpdated_cart(applyCouponBasedOnType(type, offerPerProduct, productMapList, discount,
						discountPrice, totalPrice, afterCouponApplied, 0));

			} else if (type.equals(CouponConstants.BXGY)) {
				List<Product> productDetail = details.getProduct();
				Product product = productDetail.stream().filter(x -> x.isFree() == true).findFirst().orElse(null);
				// can be handled based on null
				int productId = product.getProductId();
				updatedCartDTO.setUpdated_cart(applyCouponBasedOnType(type, 0, productMapList, discount, discountPrice,
						totalPrice, afterCouponApplied, productId));

			} else if (type.equals(CouponConstants.PRODUCT_WISE)) {
				int productId = details.getProductId();
				// Product discountProduct = productRepository.findById(productID).orElse(null);
				updatedCartDTO.setUpdated_cart(applyCouponBasedOnType(type, 0, productMapList, discount, discountPrice,
						totalPrice, afterCouponApplied, productId));
			}
		} catch (Exception e) {
			logger.error("Error in applyCouponsById(): {}", e);
		}

		return new ResponseEntity<UpdatedCartDTO>(updatedCartDTO, HttpStatus.OK);
	}

	private Map<String, Object> mapProductsAndProductMap(Map<String, Object> item, List<Product> products) {
		Map<String, Object> productMap = new HashMap<>();
		Product product = new Product();
		product.setProductId((int) item.getOrDefault(CouponConstants.PRODUCT_ID, 0));
		productMap.put(CouponConstants.PRODUCT_ID, product.getProductId());
		product.setPrice((int) item.getOrDefault(CouponConstants.PRICE, 0));
		productMap.put(CouponConstants.PRICE, product.getPrice());
		product.setQuantity((int) item.getOrDefault(CouponConstants.QUANTITY, 0));
		productMap.put(CouponConstants.QUANTITY, product.getQuantity());
		productMap.put(CouponConstants.TOTAL_DISCOUNT, 0);
		products.add(product);
		return productMap;
	}

	private AfterCouponApplied applyCouponBasedOnType(String type, double offerPerProduct,
			List<Map<String, Object>> productMapList,
			double discount, double discountPrice, double totalPrice, AfterCouponApplied afterCouponApplied,
			int productId) {
		if (type.equals(CouponConstants.CART_WISE)) {
			productMapList = productMapList.stream().map(x -> {
				int quantity = (int) x.get(CouponConstants.QUANTITY);
				x.put(CouponConstants.TOTAL_DISCOUNT, (double) (quantity * offerPerProduct));
				return x;
			}).collect(Collectors.toList());
		} else {
			productMapList = productMapList.stream().map(x -> {
				if ((int) x.get(CouponConstants.PRODUCT_ID) == productId) {
					x.put(CouponConstants.TOTAL_DISCOUNT, discount);
				}
				return x;
			}).collect(Collectors.toList());
		}
		afterCouponApplied.setItems(productMapList);
		afterCouponApplied.setTotal_discount((int) discount);
		afterCouponApplied.setFinal_price((int) discountPrice);
		afterCouponApplied.setTotal_price((int) totalPrice);
		return afterCouponApplied;
	}

	@SuppressWarnings("unchecked")
	public ResponseEntity<String> updateCouponById(int id, Map<String, Object> entity) {
		try {
			Details detail = detailsRepository.findById(id)
					.orElseThrow(() -> new EntityNotFoundException(CouponConstants.DETAILS_NOT_FOUND));
			Map<String, Object> couponDetail = (Map<String, Object>) entity.get("details");

			switch (detail.getType()) {
				case CouponConstants.CART_WISE:{					
					updateCartWise(detail, couponDetail);
					return new ResponseEntity<String>(CouponConstants.COUPON_UPDATED_SUCCESSFULLY, HttpStatus.ACCEPTED);
				}
				case CouponConstants.PRODUCT_WISE:{					
					updateProductWise(detail, couponDetail);
					return new ResponseEntity<String>(CouponConstants.COUPON_UPDATED_SUCCESSFULLY, HttpStatus.ACCEPTED);
				}
				case CouponConstants.BXGY:{					
					updateBxgy(detail, couponDetail);
					return new ResponseEntity<String>(CouponConstants.COUPON_UPDATED_SUCCESSFULLY, HttpStatus.ACCEPTED);
				}
				default:
					throw new IllegalArgumentException(CouponConstants.UNKNOWN_COUPON_TYPE + detail.getType());

			}
		} catch (Exception e) {
			logger.error("Error in updateCouponById(): {}", e);
		}

		return new ResponseEntity<String>(CouponConstants.COUPON_NOT_UPDATED_SUCCESSFULLY, HttpStatus.BAD_REQUEST);
	}

	private void updateCartWise(Details detail, Map<String, Object> couponDetail) throws Exception{
		detail.setDiscount((int) couponDetail.get(CouponConstants.DISCOUNT));
		detail.setThreshold((int) couponDetail.get(CouponConstants.THRESHOLD));
		detailsRepository.save(detail);
	}

	private void updateProductWise(Details detail, Map<String, Object> couponDetail) throws Exception{

			detail.setDiscount((int) couponDetail.get(CouponConstants.DISCOUNT));
			int productId = (int) couponDetail.get(CouponConstants.PRODUCT_ID);
			Product product = productRepository.findById(productId)
					.orElseGet(() -> new Product(productId, 0, 0, false, 0, detail));
			product.setDiscount((int) couponDetail.get(CouponConstants.DISCOUNT));
			productRepository.save(product);
			detailsRepository.save(detail);
	}

	@SuppressWarnings("unchecked")
	private void updateBxgy(Details detail, Map<String, Object> couponDetail) throws Exception{
			detail.setRepition_limit((int) couponDetail.get(CouponConstants.REPETITION_LIMIT));

			List<Map<String, Object>> buyProducts = (List<Map<String, Object>>) couponDetail
					.getOrDefault(CouponConstants.BUY_PRODUCTS, new ArrayList<>());
			List<Map<String, Object>> getProducts = (List<Map<String, Object>>) couponDetail
					.getOrDefault(CouponConstants.GET_PRODUCTS, new ArrayList<>());
			List<Product> currentProducts = new ArrayList<>(detail.getProduct());
			List<Product> toRemove = new ArrayList<>();
			for (Product product : currentProducts) {
				boolean isBuyProduct = updateProductIfExists(product, buyProducts, false);
				boolean isGetProduct = updateProductIfExists(product, getProducts, true);

				if (!isBuyProduct && !isGetProduct) {
					toRemove.add(product);
				}
			}
			for (Product product : toRemove) {
				detail.getProduct().remove(product);
				currentProducts.remove(product);
				productRepository.delete(product);
			}
			addNewProducts(currentProducts, buyProducts, false, detail);
			addNewProducts(currentProducts, getProducts, true, detail);

			logger.info("currentProducts : " + currentProducts);
			detail.setProduct(currentProducts);
			detailsRepository.save(detail);
	}

	private boolean updateProductIfExists(Product product, List<Map<String, Object>> products, boolean isFree) {
		try {
			for (Map<String, Object> productData : products) {
				if (product.getProductId() == (int) productData.get(CouponConstants.PRODUCT_ID)) {
					product.setQuantity((int) productData.get(CouponConstants.QUANTITY));
					product.setFree(isFree);
					productRepository.save(product);
					return true;
				}
			}
		} catch (Exception e) {
			logger.error("Error in updateProductIfExists(): {}", e);
		}
		return false;
	}

	private void addNewProducts(List<Product> currentProducts, List<Map<String, Object>> products, boolean isFree,
			Details detail) {
		try {
			products.forEach(productData -> {
				int productId = (int) productData.get(CouponConstants.PRODUCT_ID);
				boolean exists = currentProducts.stream()
						.anyMatch(product -> product.getProductId() == productId);
				if (!exists) {
					Product newProduct = new Product(productId, (int) productData.get(CouponConstants.QUANTITY), 0,
							isFree, 0,
							detail);
					currentProducts.add(newProduct);
					productRepository.save(newProduct);
				}
			});
		} catch (Exception e) {
			logger.error("Error in updateProductIfExists(): {}", e);
		}
	}

	public ResponseEntity<String> deleteCouponById(int id) {
		try {
			Details detail = detailsRepository.findById(id)
					.orElseThrow(() -> new IllegalArgumentException(CouponConstants.INVALID_COUPON));
			switch (detail.getType()) {
				case "cart-wise":
					return new ResponseEntity<String>(deleteCartWiseCoupon(detail), HttpStatus.OK);
				case "product-wise":
					return new ResponseEntity<String>(deleteProductWiseCoupon(detail), HttpStatus.OK);
				case "bxgy":
					return new ResponseEntity<String>(deleteBxgyCoupon(detail), HttpStatus.OK);
				default:
					throw new IllegalArgumentException(CouponConstants.UNKNOWN_COUPON_TYPE + detail.getType());
			}
		} catch (Exception e) {
			logger.error("Error in updateProductIfExists(): {}", e);
		}
		return new ResponseEntity<String>(CouponConstants.COUPON_NOT_DELETED_SUCCESSFULLY, HttpStatus.BAD_GATEWAY);
	}

	public String deleteProductWiseCoupon(Details detail) {
		try {
			Product product = productRepository.findById(detail.getProductId())
					.orElseThrow(() -> new EntityNotFoundException(CouponConstants.PRODUCT_NOT_FOUND));
			product.setDiscount(0);
			productRepository.save(product);
			detailsRepository.delete(detail);
			return CouponConstants.COUPON_DELETED_SUCCESSFULLY;
		} catch (Exception e) {
			logger.error("Error in deleteProductWiseCoupon(): {}", e);
		}
		return CouponConstants.COUPON_NOT_DELETED_SUCCESSFULLY;
	}

	public String deleteBxgyCoupon(Details detail) {
		try {
			List<Product> products = detail.getProduct();
			products = products.stream().map(x -> {
				x.setDetails(null);
				return x;
			}).collect(Collectors.toList());

			productRepository.saveAll(products);
			detail.setProduct(null);
			detailsRepository.delete(detail);
			return CouponConstants.COUPON_DELETED_SUCCESSFULLY;
		} catch (Exception e) {
			logger.error("Error in deleteBxgyCoupon(): {}", e);
		}
		return CouponConstants.COUPON_NOT_DELETED_SUCCESSFULLY;
	}

	public String deleteCartWiseCoupon(Details detail) {
		try {
			detailsRepository.delete(detail);
			return CouponConstants.COUPON_DELETED_SUCCESSFULLY;
		} catch (Exception e) {
			// TODO: handle exception
			logger.error("Error in deleteCartWiseCoupon(): {}", e);
		}
		return CouponConstants.COUPON_NOT_DELETED_SUCCESSFULLY;
	}

	public ResponseEntity<?> deleteProduct(int id) {
		try {
			// TODO Auto-generated method stub
			Product product = productRepository.findById(id)
					.orElseThrow(() -> new EntityNotFoundException(CouponConstants.PRODUCT_ID_INVALID));
			if (product.getDetails() == null) {
				productRepository.delete(product);
			} else {
				return new ResponseEntity<String>(CouponConstants.PRODUCT_ASSOCIATED_TO_COUPON,
						HttpStatus.BAD_REQUEST);
			}
			return new ResponseEntity<String>(CouponConstants.PRODUCT_DELETED_SUCCESSFULLY, HttpStatus.OK);
		} catch (Exception e) {
			// TODO: handle exception
			logger.error("Error in deleteProduct(): {}", e);
		}
		return new ResponseEntity<String>(CouponConstants.PRODUCT_NOT_DELETED, HttpStatus.BAD_REQUEST);
	}

	public ResponseEntity<?> getProducts() {
		// TODO Auto-generated method stub
		try {
			return new ResponseEntity<List<Product>>(productRepository.findAll(), HttpStatus.OK);
		} catch (Exception e) {
			logger.error("Error in deleteProduct(): {}", e);
		}
		return new ResponseEntity<String>(CouponConstants.UNABLE_TO_FETCH_PRODUCT, HttpStatus.BAD_REQUEST);
	}

}

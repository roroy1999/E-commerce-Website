package com.monk.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.monk.dto.AfterCouponApplied;
import com.monk.dto.ApplicableCoupons;
import com.monk.dto.CouponsDTO;
import com.monk.dto.UpdatedCartDTO;
import com.monk.model.Details;
import com.monk.model.Product;
import com.monk.repository.DetailsRepository;
import com.monk.repository.ProductRepository;

@Service
public class CouponService {
	DetailsRepository detailsRepository;
	
	ProductRepository productRepository;
	
	@Autowired
	private Logger logger;

	public CouponService(DetailsRepository detailsRepository,ProductRepository productRepository) {
		this.detailsRepository = detailsRepository;
		this.productRepository = productRepository;
	}

	public String savaCoupon(Map<String, Object> entity) {
		// TODO Auto-generated method stub
		
		Details details = createDetails(entity);
		return saveBasedOnType(details);
		
		
	}
	
	public List<Map<String, Object>> getAllCoupons() {
		List<Details> details = detailsRepository.findAll();
		List<Map<String, Object>> coupons = new ArrayList<>();
		for(Details detail : details) {
			String type = detail.getType();
			coupons.add(getCouponBasedOnType(detail,type));
		}
		
		return coupons;
	}
	
	public Map<String, Object> getCouponById(int id) {
		// Need Handle here
		Details detail = detailsRepository.findById(id).orElse(null);

		return getCouponBasedOnType(detail,detail.getType());
	}
	
	private Map<String, Object> getCouponBasedOnType(Details detail, String type) {
	    Map<String, Object> coupon = new HashMap<>();
	    Map<String, Object> couponDetail = new HashMap<>();
	    coupon.put("type", type);
	    coupon.put("coupon_id", detail.getCouponId());
	    switch (type) {
	        case "cart-wise":
	            couponDetail.put("threshold", detail.getThreshold());
	            couponDetail.put("discount", detail.getDiscount());
	            break;
	        case "product-wise":
	            couponDetail.put("product_id", detail.getProductId());
	            couponDetail.put("discount", detail.getDiscount());
	            break;
	        case "bxgy":
	            List<Map<String, Integer>> getProducts = new ArrayList<>();
	            List<Map<String, Integer>> buyProducts = new ArrayList<>();
	            for (Product product : detail.getProduct()) {
	                Map<String, Integer> productDetail = Map.of(
	                    "product_id", product.getProductId(),
	                    "quantity", product.getQuantity()
	                );
	                if (product.isFree()) {
	                    getProducts.add(productDetail);
	                } else {
	                    buyProducts.add(productDetail);
	                }
	            }
	            couponDetail.put("get_products", getProducts);
	            couponDetail.put("buy_products", buyProducts);
	            couponDetail.put("repition_limit", detail.getRepition_limit());
	            break;
	        default:
	            throw new IllegalArgumentException("Unknown coupon type: " + type);
	    }
	    coupon.put("details", couponDetail);
	    return coupon;
	}

	public String saveBasedOnType(Details details) {
		
		if(details.getType().equals("NoType") || (!details.getType().equals("cart-wise")
				&& !details.getType().equals("product-wise") && !details.getType().equals("bxgy"))) {
			return "Invalide Type";
		}
		else if(details.getType().equals("product-wise")&& details.getProduct()!=new ArrayList<Product>()) {
			int id = details.getProduct().get(0).getProductId();
			Product product =productRepository.findById(id).orElse(new Product());
			product.setProductId(id);
			product.setDiscount(details.getDiscount());			
			productRepository.save(product);
			details.setProductId(id);
			Details detail_old = detailsRepository.findByProductId(id).orElse(details);
			detail_old.setProduct(new ArrayList<Product>());
			detailsRepository.save(detail_old);
			return "Product Discount was Added";
		}
		
		detailsRepository.save(details);		
		return "Coupon is Added";
		
	}
	
	@SuppressWarnings("unchecked")
	public Details createDetails(Map<String, Object> entity) {
		Details details = new Details();
		String type = (String)entity.getOrDefault("type","NoType");
		Map<String, Object> detail = (Map<String, Object>) entity.getOrDefault("details",new HashMap<>());
		int discount = (int) detail.getOrDefault("discount",0);
		int threshold = (int) detail.getOrDefault("threshold",0);
		int repition_limit = (int) detail.getOrDefault("repition_limit",0);
		List<Map<String, Object>>  buyProducts = (List<Map<String, Object>>) detail.getOrDefault("buy_products",new ArrayList<>());
		List<Map<String, Object>>  getProducts = (List<Map<String, Object>>) detail.getOrDefault("get_products",new ArrayList<>());
		List<Product> products = new ArrayList<>();
		logger.info("Products are : " + products);
		details.setType(type);
		details.setRepition_limit(repition_limit);
		details.setThreshold(threshold);
		details.setDiscount(discount);
		for (Map<String, Object> item : getProducts) {
			//Product product = new Product(item.get("product_id")),item.get("quantity")), null, true,null);
			Product product = productRepository.findById((int)item.getOrDefault("product_id",0)).orElse(new Product());
			product.setProductId((int)item.getOrDefault("product_id",0));
			product.setDetails(details);
			product.setFree(true);
			product.setPrice((int) item.getOrDefault("price",0));
			product.setQuantity((int) item.getOrDefault("quantity",0));
			products.add(product);
		}
		for (Map<String, Object> item : buyProducts) {
			//Product product = new Product(item.get("product_id")),item.get("quantity")), null, true,null);
			Product product = productRepository.findById((int)item.getOrDefault("product_id",0)).orElse(new Product());
			product.setProductId((int)item.getOrDefault("product_id",0));
			product.setDetails(details);
			product.setFree(false);
			product.setPrice((int) item.getOrDefault("price",0));
			product.setQuantity((int) item.getOrDefault("quantity",0));
			products.add(product);
		}
		if((int)detail.getOrDefault("product_id",0)!=0) {
			Product product = new Product();
			int productId = (int) detail.getOrDefault("product_id",0);
			product.setProductId(productId);
			product.setDiscount(discount);
			discount=0;
			products.add(product);
		}
		details.setProduct(products);
		return details;
	}
	
	@SuppressWarnings("unchecked")
	public ApplicableCoupons applicableCoupons(Map<String, Object> entity) {
		Map<String,Object> cart = (Map<String, Object>) entity.getOrDefault("cart", new HashMap<>());
		List<Map<String,Object>> items =  (List<Map<String,Object>>) cart.getOrDefault("items", new ArrayList<>());
		//logger.info("items : " + items);
		List<Product> products = new ArrayList<>();
		for (Map<String, Object> item : items) {
			//Product product = new Product(item.get("product_id")),item.get("quantity")), null, true,null);
			Product product = new Product();
			product.setProductId((int)item.getOrDefault("product_id",0));
			product.setPrice((int) item.getOrDefault("price",0));
			product.setQuantity((int) item.getOrDefault("quantity",0));
			products.add(product);
		}
		//logger.info("products : " + products);
		ApplicableCoupons applicableCoupons = new ApplicableCoupons();


		List<CouponsDTO> couponsDTOs = new ArrayList<>();
		couponsDTOs.addAll(bxgyCoupon(products));
		couponsDTOs.add(cartWiseCoupon(products));
		couponsDTOs.addAll(productWiseCoupon(products));
		//logger.info("bxgyCoupon : " + bxgyCoupon(products));
		
		applicableCoupons.setApplicable_coupons(couponsDTOs);
		
		return applicableCoupons;
	}
	
	public CouponsDTO cartWiseCoupon(List<Product> products) {
		CouponsDTO couponsDTO = new CouponsDTO();
		List<Details> detail_old = detailsRepository.findByType("cart-wise").orElse(new ArrayList<Details>());
		logger.info("details cart wise : " + detail_old.get(0));
		Details details = detail_old.get(0);
		double total =0;
		double threshold=details.getThreshold();
		double discount=details.getDiscount();
		
		for(Product product : products) {
			double quantity = product.getQuantity();
			double price = product.getPrice();
			total += (quantity*price);
		}
		int part=0;
		if(total>threshold) {
			part = (int) ((discount / 100) * total);
//			logger.info("Total: {}, Discount Percentage: {}, Discount Amount: {}", total, discount, part);
//		    System.out.println("Total Amount: " + total);
//		    System.out.println("Discount Applied: " + part);
		}
		
		couponsDTO.setCoupon_id(details.getCouponId());
		couponsDTO.setDiscount(part);
		couponsDTO.setType(details.getType());

		return couponsDTO;
		
	}
	
	public List<CouponsDTO> bxgyCoupon(List<Product> products) {
		List<Details> details = detailsRepository.findByType("bxgy").orElse(new ArrayList<Details>());
		//ApplicableCoupons applicableCoupons = new ApplicableCoupons();
		List<CouponsDTO> couponsDTOs = new ArrayList<>();
	    Map<Integer, Details> uniqueDetailsMap = details.stream()
	        .collect(Collectors.toMap(Details::getCouponId, detail -> detail, (existing, replacement) -> existing));

	    List<Details> uniqueDetails = new ArrayList<>(uniqueDetailsMap.values());
	    //logger.info("uniqueDetails: {} : ", uniqueDetails);
		for(Product product : products) {
			
			Product product_original = productRepository.findById(product.getProductId()).orElse(new Product());
			if(!product_original.isFree()) {				
				Details productDetails = product_original.getDetails();
				Details presentDetails= new Details();
				for(Details det :uniqueDetails) {
					if(det.getCouponId()==productDetails.getCouponId()) {
						presentDetails = det;
						break;
					}
				}
				//List<Product> productList =  presentDetails.getProduct();
				int inCartProductQuantity = product.getQuantity();
				//int inCartProductprice = product.getPrice();
				int acceptedProductQuntity = product_original.getQuantity();
				if(presentDetails.getRepition_limit()>0 && inCartProductQuantity>= acceptedProductQuntity) {
					presentDetails.setRepition_limit(presentDetails.getRepition_limit() - (inCartProductQuantity/acceptedProductQuntity));
					List<Product> freeProducts = presentDetails.getProduct();
					Product freeProduct = freeProducts.stream().filter(x->x.isFree()).findFirst().orElse(null);
					Product productCart = products.stream().filter(x->x.getProductId()==freeProduct.getProductId()).findFirst().orElse(null);
					int freePrice =productCart.getPrice();
					int discount = (inCartProductQuantity/acceptedProductQuntity)*freePrice;
					final int id = presentDetails.getCouponId();
					CouponsDTO couponsDTO_Old = couponsDTOs.stream().filter(x->x.getCoupon_id()==id).findFirst().orElse(null);
					if(couponsDTO_Old==null) {
						CouponsDTO couponsDTO = new CouponsDTO();
						couponsDTO.setType("bxgy");
						couponsDTO.setDiscount(discount);
						couponsDTO.setCoupon_id(presentDetails.getCouponId());
						couponsDTOs.add(couponsDTO);
					}else {						
						couponsDTO_Old.setDiscount(couponsDTO_Old.getDiscount() + discount);
					}
				}
			}
		}
		return couponsDTOs;
	}

	public List<CouponsDTO> productWiseCoupon(List<Product> products) {
		List<CouponsDTO> couponsDTOs = new ArrayList<>();
		for(Product productCart : products) {
			Details details = detailsRepository.findByProductId(productCart.getProductId()).orElse(new Details());
			Product product_original = productRepository.findById(productCart.getProductId()).orElse(new Product());
			double discount = product_original.getDiscount();
			if(discount>0) {
				double total = 0.0;
				double quantity = productCart.getQuantity();
				double price = productCart.getPrice();
				
				total += (quantity*price);
				int discountAmount = (int)((discount/100)*total);
				CouponsDTO couponsDTO = new CouponsDTO();
				couponsDTO.setCoupon_id(details.getCouponId());
				couponsDTO.setDiscount(discountAmount);
				couponsDTO.setType(details.getType());
				couponsDTOs.add(couponsDTO);
			}
			
		}
		
		return couponsDTOs;
	}

	@SuppressWarnings("unchecked")
	public UpdatedCartDTO applyCouponsById(Map<String, Object> entity, int id) {
		Map<String,Object> cart = (Map<String, Object>) entity.getOrDefault("cart", new HashMap<>());
		List<Map<String,Object>> items =  (List<Map<String,Object>>) cart.getOrDefault("items", new ArrayList<>());
		List<Map<String, Object>> productMapList = new ArrayList<>();
		List<Product> products = new ArrayList<>();
		for (Map<String, Object> item : items) {
			Map<String, Object> productMap = new HashMap<>();
			Product product = new Product();
			product.setProductId((int)item.getOrDefault("product_id",0));
			productMap.put("product_id",product.getProductId());
			product.setPrice((int) item.getOrDefault("price",0));
			productMap.put("price",product.getPrice());
			product.setQuantity((int) item.getOrDefault("quantity",0));
			productMap.put("quantity",product.getQuantity());
			productMap.put("total_discount", 0);
			products.add(product);
			productMapList.add(productMap);
		}
		ApplicableCoupons applicableCoupons = applicableCoupons(entity);
		List<CouponsDTO> coupons =  applicableCoupons.getApplicable_coupons();
		
		CouponsDTO coupon = coupons.stream().filter(x->x.getCoupon_id()==id).findFirst().orElse(null);
		double totalPrice = products.stream().reduce(0.0,(c,y)->c+(y.getPrice()*y.getQuantity()),Double::sum);
//		logger.info("Total price : " + totalPrice);
		double discount = coupon.getDiscount();
		double discountPrice = totalPrice-discount;
		String type = coupon.getType();
		AfterCouponApplied afterCouponApplied = new AfterCouponApplied();
		UpdatedCartDTO updatedCartDTO = new UpdatedCartDTO();
		Details details = detailsRepository.findById(coupon.getCoupon_id()).orElse(new Details());
		if(type.equals("cart-wise")){
			//int size = details.getProduct().size();
			int totalProduct = products.stream().reduce(0,(c,y)->c+(y.getQuantity()),Integer::sum);
			double offerPerProduct = discount/totalProduct;
			productMapList= productMapList.stream().map(x->{
				int quantity = (int) x.get("quantity");
		        x.put("total_discount", (double)(quantity*offerPerProduct));
		        return x; // Return the modified map
		    }).collect(Collectors.toList());
			afterCouponApplied.setItems(productMapList);
			afterCouponApplied.setTotal_discount((int)discount);
			afterCouponApplied.setFinal_price((int)discountPrice);
			afterCouponApplied.setTotal_price((int)totalPrice);
			
			updatedCartDTO.setUpdated_cart(afterCouponApplied);
			
		} else if(type.equals("bxgy")) {
			List<Product> productDetail = details.getProduct();
			Product product = productDetail.stream().filter(x->x.isFree()==true).findFirst().orElse(null);
			//can be handled based on null
			int productId = product.getProductId();
			productMapList= productMapList.stream().map(x->{				
				if((int)x.get("product_id")==productId) {
					x.put("total_discount", discount);
				}
				return x; 
		    }).collect(Collectors.toList());
			
			afterCouponApplied.setItems(productMapList);
			afterCouponApplied.setTotal_discount((int)discount);
			afterCouponApplied.setFinal_price((int)discountPrice);
			afterCouponApplied.setTotal_price((int)totalPrice);
			
			updatedCartDTO.setUpdated_cart(afterCouponApplied);
			
			
		} else if(type.equals("product-wise")) {
			int productId = details.getProductId();
			//Product discountProduct = productRepository.findById(productID).orElse(null);
			productMapList= productMapList.stream().map(x->{				
				if((int)x.get("product_id")==productId) {
					x.put("total_discount", discount);
				}
				return x; 
		    }).collect(Collectors.toList());
			
			afterCouponApplied.setItems(productMapList);
			afterCouponApplied.setTotal_discount((int)discount);
			afterCouponApplied.setFinal_price((int)discountPrice);
			afterCouponApplied.setTotal_price((int)totalPrice);
			
			updatedCartDTO.setUpdated_cart(afterCouponApplied);
		}
		
		return updatedCartDTO;
	}

}

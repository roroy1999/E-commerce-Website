package com.monk.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
	
	public List<Details> getAllCoupons() {
		return detailsRepository.findAll();
	}

}

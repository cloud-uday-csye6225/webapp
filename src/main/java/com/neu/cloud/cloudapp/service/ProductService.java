package com.neu.cloud.cloudapp.service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.neu.cloud.cloudapp.Utils.AuthHandler;
import com.neu.cloud.cloudapp.Utils.Utils;
import com.neu.cloud.cloudapp.model.Product;
import com.neu.cloud.cloudapp.model.User;
import com.neu.cloud.cloudapp.repository.ProductRepository;

import jakarta.servlet.http.HttpServletRequest;

@Service
public class ProductService {

	@Autowired
	private ProductRepository productRepository;

	@Autowired
	private AuthHandler authHandler;

	public ResponseEntity<Map<String, Object>> create(Map<String, String> requMap,
			HttpServletRequest httpServletRequest) throws DataIntegrityViolationException {
		User authUser = authHandler.getUser(httpServletRequest);
		if (authUser == null) {
			return new ResponseEntity<>(null, HttpStatusCode.valueOf(401));
		}

		if (requMap.containsKey("name") == false || requMap.containsKey("description") == false
				|| requMap.containsKey("sku") == false || requMap.containsKey("manufacturer") == false
				|| requMap.containsKey("quantity") == false) {
			return new ResponseEntity<>(null, HttpStatusCode.valueOf(400));
		}

		String name = requMap.getOrDefault("name", null);
		String description = requMap.getOrDefault("description", null);
		String sku = requMap.getOrDefault("sku", null);
		String manufacturer = requMap.getOrDefault("manufacturer", null);
		String quantity = requMap.getOrDefault("quantity", null);
		if (Utils.isValidString(name) == false || Utils.isValidString(description) == false
				|| Utils.isValidString(sku) == false || Utils.isValidString(manufacturer) == false
				|| Utils.isValidNumber(quantity) == false) {
			return new ResponseEntity<>(null, HttpStatusCode.valueOf(400));
		}

		int qty = Integer.parseInt(quantity);

		if (qty < 0 || qty > 100) {
			return new ResponseEntity<>(null, HttpStatusCode.valueOf(400));
		}

		Product product = new Product();
		product.setName(name);
		product.setQuantity(qty);
		product.setManufacturer(manufacturer);
		product.setSku(sku);
		product.setDescription(description);
		product.setDateAdded(LocalDateTime.now().toString());
		product.setDateLastUpdated(LocalDateTime.now().toString());
		product.setUser(authUser);

		productRepository.save(product);
		return new ResponseEntity<>(convertToProductDto(product), HttpStatusCode.valueOf(200));
	}

	private Map<String, Object> convertToProductDto(Product product) {
		ObjectMapper mapper = new ObjectMapper();
		@SuppressWarnings("unchecked")
		Map<String, Object> map = mapper.convertValue(product, Map.class);
		map.remove("user");
		map.remove("dateAdded");
		map.remove("dateLastUpdated");
		map.put("date_added", product.getDateAdded());
		map.put("date_last_updated", product.getDateLastUpdated());
		map.put("owner_user_id", product.getUser().getId());
		return map;
	}

	public ResponseEntity<Map<String, Object>> fetchProductById(String productId,
			HttpServletRequest httpServletRequest) {

		if (Utils.isValidNumber(productId) == false) {
			return new ResponseEntity<>(null, HttpStatusCode.valueOf(400));
		}

		Optional<Product> produOptional = productRepository.findById(Integer.parseInt(productId));

		if (!produOptional.isPresent()) {
			return new ResponseEntity<>(null, HttpStatusCode.valueOf(404));
		}
		return new ResponseEntity<>(convertToProductDto(produOptional.get()), HttpStatusCode.valueOf(200));
	}

	public ResponseEntity<Map<String, Object>> deleteProductById(String productId,
			HttpServletRequest httpServletRequest) {
		User authUser = authHandler.getUser(httpServletRequest);
		if (authUser == null) {
			return new ResponseEntity<>(null, HttpStatusCode.valueOf(401));
		}

		if (Utils.isValidNumber(productId) == false) {
			return new ResponseEntity<>(null, HttpStatusCode.valueOf(400));
		}

		Optional<Product> produOptional = productRepository.findById(Integer.parseInt(productId));

		if (!produOptional.isPresent()) {
			return new ResponseEntity<>(null, HttpStatusCode.valueOf(404));
		}

		if (produOptional.get().getUser().getId() != authUser.getId()) {
			return new ResponseEntity<>(null, HttpStatusCode.valueOf(403));
		}

		productRepository.delete(produOptional.get());
		return new ResponseEntity<>(convertToProductDto(produOptional.get()), HttpStatusCode.valueOf(204));
	}

	public ResponseEntity<Map<String, Object>> updateProductById(String productId, Map<String, String> requMap,
			HttpServletRequest httpServletRequest) {
		User authUser = authHandler.getUser(httpServletRequest);
		if (authUser == null) {
			return new ResponseEntity<>(null, HttpStatusCode.valueOf(401));
		}

		if (Utils.isValidNumber(productId) == false) {
			return new ResponseEntity<>(null, HttpStatusCode.valueOf(400));
		}

		Optional<Product> produOptional = productRepository.findById(Integer.parseInt(productId));

		if (!produOptional.isPresent()) {
			return new ResponseEntity<>(null, HttpStatusCode.valueOf(404));
		}

		if (produOptional.get().getUser().getId() != authUser.getId()) {
			return new ResponseEntity<>(null, HttpStatusCode.valueOf(403));
		}

		Product product = produOptional.get();

		if (requMap.containsKey("sku")) {
			return new ResponseEntity<>(null, HttpStatusCode.valueOf(400));
		}

		String name = requMap.getOrDefault("name", null);
		String description = requMap.getOrDefault("description", null);
		String manufacturer = requMap.getOrDefault("manufacturer", null);

		if (Utils.isValidString(name)) {
			product.setName(name);
			product.setDateLastUpdated(LocalDateTime.now().toString());
		}

		if (Utils.isValidString(description)) {
			product.setDescription(description);
			product.setDateLastUpdated(LocalDateTime.now().toString());
		}

		if (Utils.isValidString(manufacturer)) {
			product.setManufacturer(manufacturer);
			product.setDateLastUpdated(LocalDateTime.now().toString());
		}
		System.out.println("out qty");

		if (requMap.containsKey("quantity")) {
			System.out.println("in qty");
			String quantity = requMap.getOrDefault("quantity", null);
			if (Utils.isValidNumber(quantity)) {
				System.out.println("in qty check number");
				int qty = Integer.parseInt(quantity);
				if (qty >= 0 && qty <= 100) {
					System.out.println("in qty check number range");
					product.setQuantity(qty);
					product.setDateLastUpdated(LocalDateTime.now().toString());
				} else {
					System.out.println("out qty check number range");
					return new ResponseEntity<>(null, HttpStatusCode.valueOf(400));
				}
			} else {
				System.out.println("out qty check number");
				return new ResponseEntity<>(null, HttpStatusCode.valueOf(400));
			}
		}
		productRepository.save(product);
		return new ResponseEntity<>(null, HttpStatusCode.valueOf(204));
	}

}

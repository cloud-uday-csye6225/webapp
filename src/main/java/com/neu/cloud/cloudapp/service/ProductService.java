package com.neu.cloud.cloudapp.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.neu.cloud.cloudapp.Utils.AuthHandler;
import com.neu.cloud.cloudapp.Utils.Utils;
import com.neu.cloud.cloudapp.model.Image;
import com.neu.cloud.cloudapp.model.Product;
import com.neu.cloud.cloudapp.model.User;
import com.neu.cloud.cloudapp.repository.ImageRepository;
import com.neu.cloud.cloudapp.repository.ProductRepository;

import jakarta.servlet.http.HttpServletRequest;

@Service
public class ProductService {

	Logger logger = LoggerFactory.getLogger(ProductService.class);

	@Autowired
	private ProductRepository productRepository;

	@Autowired
	private AuthHandler authHandler;

	@Autowired
	private ImageRepository imageRepository;

	@Autowired
	private ImageService imageService;

	public ResponseEntity<Map<String, Object>> create(Map<String, Object> requMap,
			HttpServletRequest httpServletRequest) throws DataIntegrityViolationException {
		User authUser = authHandler.getUser(httpServletRequest);
		if (authUser == null) {
			logger.error("Unauthorized attempt to access product");
			return new ResponseEntity<>(null, HttpStatusCode.valueOf(401));
		}

		if (requMap.containsKey("name") == false || requMap.containsKey("description") == false
				|| requMap.containsKey("sku") == false || requMap.containsKey("manufacturer") == false
				|| requMap.containsKey("quantity") == false) {
			logger.error("product creation does not have all required fields");
			return new ResponseEntity<>(null, HttpStatusCode.valueOf(400));
		}

		String name = (String) requMap.getOrDefault("name", null);
		String description = (String) requMap.getOrDefault("description", null);
		String sku = (String) requMap.getOrDefault("sku", null);
		String manufacturer = (String) requMap.getOrDefault("manufacturer", null);
		int quantity = (int) requMap.getOrDefault("quantity", null);
		if (Utils.isValidString(name) == false || Utils.isValidString(description) == false
				|| Utils.isValidString(sku) == false || Utils.isValidString(manufacturer) == false) {
			logger.error("product creation does not have all valid values");
			return new ResponseEntity<>(null, HttpStatusCode.valueOf(400));
		}

		int qty = quantity;

		if (qty < 0 || qty > 100) {
			logger.error("product quantity should be btw 0 and 100");
			return new ResponseEntity<>(null, HttpStatusCode.valueOf(400));
		}

		Product product = new Product();
		product.setName(name.trim());
		product.setQuantity(qty);
		product.setManufacturer(manufacturer.trim());
		product.setSku(sku.trim());
		product.setDescription(description.trim());
		product.setDateAdded(LocalDateTime.now().toString());
		product.setDateLastUpdated(LocalDateTime.now().toString());
		product.setUser(authUser);

		productRepository.save(product);
		logger.info("product created successfully with id " + product.getId());
		return new ResponseEntity<>(convertToProductDto(product), HttpStatusCode.valueOf(201));
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
			logger.error("product id should be valid integer given: " + productId);
			return new ResponseEntity<>(null, HttpStatusCode.valueOf(400));
		}

		Optional<Product> produOptional = productRepository.findById(Integer.parseInt(productId));

		if (!produOptional.isPresent()) {
			logger.error("product does not exist with given: " + productId);
			return new ResponseEntity<>(null, HttpStatusCode.valueOf(404));
		}
		logger.error("product fetched successfully with given: " + productId);
		return new ResponseEntity<>(convertToProductDto(produOptional.get()), HttpStatusCode.valueOf(200));
	}

	public ResponseEntity<Map<String, Object>> deleteProductById(String productId,
			HttpServletRequest httpServletRequest) {
		User authUser = authHandler.getUser(httpServletRequest);
		if (authUser == null) {
			logger.error("Unauthorized attempt to access product");
			return new ResponseEntity<>(null, HttpStatusCode.valueOf(401));
		}

		if (Utils.isValidNumber(productId) == false) {
			logger.error("product id should be valid integer given: " + productId);
			return new ResponseEntity<>(null, HttpStatusCode.valueOf(400));
		}

		Optional<Product> produOptional = productRepository.findById(Integer.parseInt(productId));

		if (!produOptional.isPresent()) {
			logger.error("product does not exist with given: " + productId);
			return new ResponseEntity<>(null, HttpStatusCode.valueOf(404));
		}

		if (produOptional.get().getUser().getId() != authUser.getId()) {
			logger.error("Forbidden attempt to access product" + productId + "by user " + authUser.getId());
			return new ResponseEntity<>(null, HttpStatusCode.valueOf(403));
		}

		try {
			List<Image> images = imageRepository.findAllByProductId(Integer.parseInt(productId));
			for (Image img : images) {
				imageService.deleteImage(String.valueOf(img.getId()), productId, httpServletRequest);
			}
		} catch (Exception e) {
			logger.error("error while deleting product " + productId);
		}
		productRepository.delete(produOptional.get());
		logger.info("product deleted successfully " + productId);
		return new ResponseEntity<>(convertToProductDto(produOptional.get()), HttpStatusCode.valueOf(204));
	}

	public ResponseEntity<Map<String, Object>> patchOperationUpdateProductById(String productId,
			Map<String, Object> requMap, HttpServletRequest httpServletRequest) {
		User authUser = authHandler.getUser(httpServletRequest);
		if (authUser == null) {
			logger.error("Unauthorized attempt to access product");
			return new ResponseEntity<>(null, HttpStatusCode.valueOf(401));
		}

		if (Utils.isValidNumber(productId) == false) {
			logger.error("product id should be valid integer given: " + productId);
			return new ResponseEntity<>(null, HttpStatusCode.valueOf(400));
		}

		Optional<Product> produOptional = productRepository.findById(Integer.parseInt(productId));

		if (!produOptional.isPresent()) {
			logger.error("product does not exist with given: " + productId);
			return new ResponseEntity<>(null, HttpStatusCode.valueOf(404));
		}

		if (produOptional.get().getUser().getId() != authUser.getId()) {
			logger.error("Forbidden attempt to access product" + productId + "by user " + authUser.getId());
			return new ResponseEntity<>(null, HttpStatusCode.valueOf(403));
		}

		Product product = produOptional.get();

		String name = (String) requMap.getOrDefault("name", null);
		String description = (String) requMap.getOrDefault("description", null);
		String manufacturer = (String) requMap.getOrDefault("manufacturer", null);
		String sku = (String) requMap.getOrDefault("sku", null);

		if (Utils.isValidString(sku)) {
			product.setSku(sku.trim());
			product.setDateLastUpdated(LocalDateTime.now().toString());
		}

		if (Utils.isValidString(name)) {
			product.setName(name.trim());
			product.setDateLastUpdated(LocalDateTime.now().toString());
		}

		if (Utils.isValidString(description)) {
			product.setDescription(description.trim());
			product.setDateLastUpdated(LocalDateTime.now().toString());
		}

		if (Utils.isValidString(manufacturer)) {
			product.setManufacturer(manufacturer.trim());
			product.setDateLastUpdated(LocalDateTime.now().toString());
		}

		if (requMap.containsKey("quantity")) {
			int qty = (int) requMap.getOrDefault("quantity", null);
			if (qty >= 0 && qty <= 100) {
				product.setQuantity(qty);
				product.setDateLastUpdated(LocalDateTime.now().toString());
			} else {
				logger.error("product quantity should be btw 0 and 100");
				return new ResponseEntity<>(null, HttpStatusCode.valueOf(400));
			}
		}
		logger.info("product patch updated successfully " + productId);
		productRepository.save(product);
		return new ResponseEntity<>(null, HttpStatusCode.valueOf(204));
	}

	public ResponseEntity<Map<String, Object>> updateProductById(String productId, Map<String, Object> requMap,
			HttpServletRequest httpServletRequest) {
		User authUser = authHandler.getUser(httpServletRequest);
		if (authUser == null) {
			logger.error("Unauthorized attempt to access product");
			return new ResponseEntity<>(null, HttpStatusCode.valueOf(401));
		}

		if (Utils.isValidNumber(productId) == false) {
			logger.error("product id should be valid integer given: " + productId);
			return new ResponseEntity<>(null, HttpStatusCode.valueOf(400));
		}

		Optional<Product> produOptional = productRepository.findById(Integer.parseInt(productId));

		if (!produOptional.isPresent()) {
			logger.error("product does not exist with given: " + productId);
			return new ResponseEntity<>(null, HttpStatusCode.valueOf(404));
		}

		if (produOptional.get().getUser().getId() != authUser.getId()) {
			logger.error("Forbidden attempt to access product" + productId + "by user " + authUser.getId());
			return new ResponseEntity<>(null, HttpStatusCode.valueOf(403));
		}

		Product product = produOptional.get();

		if (requMap.containsKey("name") == false || requMap.containsKey("description") == false
				|| requMap.containsKey("sku") == false || requMap.containsKey("manufacturer") == false
				|| requMap.containsKey("quantity") == false) {
			logger.error("product creation does not have all required fields");
			return new ResponseEntity<>(null, HttpStatusCode.valueOf(400));
		}

		String name = (String) requMap.getOrDefault("name", null);
		String description = (String) requMap.getOrDefault("description", null);
		String sku = (String) requMap.getOrDefault("sku", null);
		String manufacturer = (String) requMap.getOrDefault("manufacturer", null);
		int qty = (int) requMap.getOrDefault("quantity", null);

		if (Utils.isValidString(name) == false || Utils.isValidString(description) == false
				|| Utils.isValidString(sku) == false || Utils.isValidString(manufacturer) == false) {
			logger.error("product creation does not have all valid values");
			return new ResponseEntity<>(null, HttpStatusCode.valueOf(400));
		}

		if (qty < 0 || qty > 100) {
			logger.error("product quantity should be btw 0 and 100");
			return new ResponseEntity<>(null, HttpStatusCode.valueOf(400));
		}

		if (Utils.isValidString(sku)) {
			product.setSku(sku.trim());
			product.setDateLastUpdated(LocalDateTime.now().toString());
		}

		if (Utils.isValidString(name)) {
			product.setName(name.trim());
			product.setDateLastUpdated(LocalDateTime.now().toString());
		}

		if (Utils.isValidString(description)) {
			product.setDescription(description.trim());
			product.setDateLastUpdated(LocalDateTime.now().toString());
		}

		if (Utils.isValidString(manufacturer)) {
			product.setManufacturer(manufacturer.trim());
			product.setDateLastUpdated(LocalDateTime.now().toString());
		}

		if (qty >= 0 && qty <= 100) {
			product.setQuantity(qty);
			product.setDateLastUpdated(LocalDateTime.now().toString());
		}

		productRepository.save(product);
		logger.info("product patch updated successfully " + productId + " for user " + authUser.getId());
		return new ResponseEntity<>(null, HttpStatusCode.valueOf(204));

	}

}

package com.neu.cloud.cloudapp.controller;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.neu.cloud.cloudapp.service.ProductService;
import com.timgroup.statsd.StatsDClient;

import jakarta.servlet.http.HttpServletRequest;

@RestController
public class ProductController {

	Logger logger = LoggerFactory.getLogger(ProductController.class);

	@Autowired
	private ProductService productService;

	@Autowired
	private StatsDClient statsDClient;

	private String postCreateProductString = "post.createproduct.count";
	private String getProductString = "get.product.count";
	private String putProductString = "put.product.count";
	private String patchProductString = "patch.product.count";
	private String deleteProductString = "delete.product.count";

	@PostMapping("/v1/product")
	public ResponseEntity<Map<String, Object>> createProduct(@RequestBody(required = false) Map<String, Object> requMap,
			HttpServletRequest httpServletRequest) {
		try {
			statsDClient.incrementCounter(postCreateProductString);
			logger.info("creating product data for user");
			return productService.create(requMap, httpServletRequest);
		} catch (Exception e) {
			Map<String, Object> resMap = new HashMap<>();
			resMap.put("msg", "Please enter valid input");
			logger.error("error while creating product data" + e.getMessage());
			return new ResponseEntity<>(resMap, HttpStatusCode.valueOf(400));
		}
	}

	@GetMapping("/v1/product/{productId}")
	public ResponseEntity<Map<String, Object>> getProductById(@PathVariable String productId,
			HttpServletRequest httpServletRequest) {
		try {
			statsDClient.incrementCounter(getProductString);
			logger.info("fetching product data for user");
			return productService.fetchProductById(productId, httpServletRequest);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			Map<String, Object> resMap = new HashMap<>();
			resMap.put("msg", "Please enter valid data");
			logger.error("error while fetching product data" + e.getMessage());
			return new ResponseEntity<>(resMap, HttpStatusCode.valueOf(400));
		}
	}

	@DeleteMapping("/v1/product/{productId}")
	public ResponseEntity<Map<String, Object>> deleteProduct(@PathVariable String productId,
			HttpServletRequest httpServletRequest) {
		try {
			statsDClient.incrementCounter(deleteProductString);
			logger.info("deleting product data for user");
			return productService.deleteProductById(productId, httpServletRequest);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			Map<String, Object> resMap = new HashMap<>();
			resMap.put("msg", "Please enter valid data");
			logger.error("error while deleting product data" + e.getMessage());
			return new ResponseEntity<>(resMap, HttpStatusCode.valueOf(400));
		}
	}

	@PutMapping("/v1/product/{productId}")
	public ResponseEntity<Map<String, Object>> updateProductById(@PathVariable String productId,
			@RequestBody Map<String, Object> requMap, HttpServletRequest httpServletRequest) {
		try {
			statsDClient.incrementCounter(putProductString);
			logger.info("updating product data for user");
			return productService.updateProductById(productId, requMap, httpServletRequest);
		} catch (Exception e) {
			logger.error("error while updating product data" + e.getMessage());
			return new ResponseEntity<>(null, HttpStatusCode.valueOf(400));
		}
	}

	@PatchMapping("/v1/product/{productId}")
	public ResponseEntity<Map<String, Object>> updateOrPatchProductById(@PathVariable String productId,
			@RequestBody Map<String, Object> requMap, HttpServletRequest httpServletRequest) {
		try {
			statsDClient.incrementCounter(patchProductString);
			logger.info("patch operation for  product data for user");
			return productService.patchOperationUpdateProductById(productId, requMap, httpServletRequest);
		} catch (Exception e) {
			logger.error("error while patch operation on product data" + e.getMessage());
			return new ResponseEntity<>(null, HttpStatusCode.valueOf(400));
		}
	}

}

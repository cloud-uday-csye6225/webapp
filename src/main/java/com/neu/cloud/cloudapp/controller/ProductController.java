package com.neu.cloud.cloudapp.controller;

import java.util.HashMap;
import java.util.Map;

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

import jakarta.servlet.http.HttpServletRequest;

@RestController
public class ProductController {

	@Autowired
	private ProductService productService;

	@PostMapping("/v1/product")
	public ResponseEntity<Map<String, Object>> createProduct(@RequestBody(required = false) Map<String, Object> requMap,
			HttpServletRequest httpServletRequest) {
		try {
			return productService.create(requMap, httpServletRequest);
		} catch (Exception e) {
			Map<String, Object> resMap = new HashMap<>();
			resMap.put("msg", "Please enter valid input");
			return new ResponseEntity<>(resMap, HttpStatusCode.valueOf(400));
		}
	}

	@GetMapping("/v1/product/{productId}")
	public ResponseEntity<Map<String, Object>> getProductById(@PathVariable String productId,
			HttpServletRequest httpServletRequest) {
		try {
			return productService.fetchProductById(productId, httpServletRequest);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			Map<String, Object> resMap = new HashMap<>();
			resMap.put("msg", "Please enter valid data");
			return new ResponseEntity<>(resMap, HttpStatusCode.valueOf(400));
		}
	}

	@DeleteMapping("/v1/product/{productId}")
	public ResponseEntity<Map<String, Object>> deleteProduct(@PathVariable String productId,
			HttpServletRequest httpServletRequest) {
		try {
			return productService.deleteProductById(productId, httpServletRequest);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			Map<String, Object> resMap = new HashMap<>();
			resMap.put("msg", "Please enter valid data");
			return new ResponseEntity<>(resMap, HttpStatusCode.valueOf(400));
		}
	}

	@PutMapping("/v1/product/{productId}")
	public ResponseEntity<Map<String, Object>> updateProductById(@PathVariable String productId,
			@RequestBody Map<String, Object> requMap, HttpServletRequest httpServletRequest) {
		try {
			return productService.updateProductById(productId, requMap, httpServletRequest);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			return new ResponseEntity<>(null, HttpStatusCode.valueOf(400));
		}
	}

	@PatchMapping("/v1/product/{productId}")
	public ResponseEntity<Map<String, Object>> updateOrPatchProductById(@PathVariable String productId,
			@RequestBody Map<String, Object> requMap, HttpServletRequest httpServletRequest) {
		try {
			return productService.patchOperationUpdateProductById(productId, requMap, httpServletRequest);
		} catch (Exception e) {
			return new ResponseEntity<>(null, HttpStatusCode.valueOf(400));
		}
	}

}

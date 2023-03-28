package com.neu.cloud.cloudapp.service;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.neu.cloud.cloudapp.Utils.AuthHandler;
import com.neu.cloud.cloudapp.Utils.Utils;
import com.neu.cloud.cloudapp.controller.ImageController;
import com.neu.cloud.cloudapp.model.Image;
import com.neu.cloud.cloudapp.model.Product;
import com.neu.cloud.cloudapp.model.User;
import com.neu.cloud.cloudapp.repository.ImageRepository;
import com.neu.cloud.cloudapp.repository.ProductRepository;

import jakarta.servlet.http.HttpServletRequest;

@Service
public class ImageService {

	Logger logger = LoggerFactory.getLogger(ImageService.class);

	@Autowired
	private ImageRepository imageRepository;

	@Autowired
	private ProductRepository productRepository;

	@Autowired
	private AuthHandler authHandler;

	@Autowired
	private AmazonS3 s3;

	@Value("${aws.s3.bucket}")
	private String s3Bucket;

	public ResponseEntity<?> createImage(MultipartFile multipartFile, String productId,
			HttpServletRequest httpServletRequest) {
		try {

			User authUser = authHandler.getUser(httpServletRequest);
			if (authUser == null) {
				logger.error("Unauthorized attempt to access image");
				return new ResponseEntity<>(null, HttpStatusCode.valueOf(401));
			}

			if (Utils.isValidNumber(productId) == false) {
				logger.error("Product Id should be a valid integer given is " + productId);
				return new ResponseEntity<>(null, HttpStatusCode.valueOf(400));
			}

			Optional<Product> produOptional = productRepository.findById(Integer.parseInt(productId));

			if (!produOptional.isPresent()) {
				logger.error("Product does not exist with id as " + productId);
				return new ResponseEntity<>(null, HttpStatusCode.valueOf(404));
			}

			if (produOptional.get().getUser().getId() != authUser.getId()) {
				logger.error("Forbidden attempt to access product" + productId + "by user " + authUser.getId());
				return new ResponseEntity<>(null, HttpStatusCode.valueOf(403));
			}

			Product product = produOptional.get();
			String extension = FilenameUtils.getExtension(multipartFile.getOriginalFilename());
			if (!isSupportedExtension(extension)) {
				logger.error("Only images with  jpg, png, jpeg is supported");
				return new ResponseEntity<>(null, HttpStatusCode.valueOf(400));
			}

			InputStream fis = multipartFile.getInputStream();

			String filename = multipartFile.getOriginalFilename();
			String filepath = UUID.randomUUID() + "/" + filename;
			String bucket = s3Bucket;
			System.out.println(bucket);
			System.out.println("filename " + filename);
			System.out.println("extension " + extension);

			ObjectMetadata objectMetadata = new ObjectMetadata();
			objectMetadata.setContentLength(multipartFile.getSize());
			objectMetadata.setContentType(multipartFile.getContentType());
			objectMetadata.setCacheControl("public, max-age=31536000");

			try {
				s3.putObject(bucket, filepath, fis, objectMetadata);
			} catch (AmazonServiceException e) {
				logger.error("s3 put object request failed for image for product " + productId);
				return new ResponseEntity<>(null, HttpStatusCode.valueOf(400));
			}

			String imageUrl = String.valueOf(s3.getUrl(bucket, filepath));
			logger.info("s3 object url " + imageUrl);
			System.out.println(imageUrl);
			Image image = new Image(product.getId(), filename, LocalDateTime.now().toString(), imageUrl);
			imageRepository.save(image);
			logger.info("image created with id " + image.getId() + " for product " + product.getId());
			return new ResponseEntity<>(convertToImageDto(new ArrayList<>(Arrays.asList(image))),
					HttpStatusCode.valueOf(201));
		} catch (Exception e) {
			logger.error("error while creating image data" + e.getMessage());
			return new ResponseEntity<>(null, HttpStatusCode.valueOf(400));
		}
	}

	private boolean isSupportedExtension(String extension) {
		return extension != null && (extension.equalsIgnoreCase("png") || extension.equalsIgnoreCase("jpg")
				|| extension.equalsIgnoreCase("jpeg"));
	}

	public ResponseEntity<?> getAllImage(String productId, HttpServletRequest httpServletRequest) {
		User authUser = authHandler.getUser(httpServletRequest);
		if (authUser == null) {
			logger.error("Unauthorized attempt to access images");
			return new ResponseEntity<>(null, HttpStatusCode.valueOf(401));
		}

		if (Utils.isValidNumber(productId) == false) {
			logger.error("Product Id should be a valid integer given is " + productId);
			return new ResponseEntity<>(null, HttpStatusCode.valueOf(400));
		}

		Optional<Product> produOptional = productRepository.findById(Integer.parseInt(productId));

		if (!produOptional.isPresent()) {
			logger.error("Product does not exist with id as " + productId);
			return new ResponseEntity<>(null, HttpStatusCode.valueOf(404));
		}

		if (produOptional.get().getUser().getId() != authUser.getId()) {
			logger.error("Forbidden attempt to access product" + productId + "by user " + authUser.getId());
			return new ResponseEntity<>(null, HttpStatusCode.valueOf(403));
		}

		List<Image> images = imageRepository.findAllByProductId(Integer.parseInt(productId));

		logger.info("images fetched for product " + productId);
		if (images != null && !images.isEmpty()) {
			logger.info("images fetched for product " + productId + " of size " + images.size());
			return new ResponseEntity<>(convertToImageDto(images), HttpStatusCode.valueOf(200));
		}
		logger.info("No images fetched for product " + productId);
		return new ResponseEntity<>(null, HttpStatusCode.valueOf(200));

	}

	private List<Object> convertToImageDto(List<Image> images) {
		List<Object> objsList = new ArrayList<>();
		for (Image img : images) {
			Map<String, Object> map = new HashMap<>();
			map.put("image_id", img.getId());
			map.put("product_id", img.getProductId());
			map.put("file_name", img.getFileName());
			map.put("date_created", img.getDateCreated());
			map.put("s3_bucket_path", img.getS3BucketPath());
			objsList.add(map);
		}
		return objsList;
	}

	public ResponseEntity<?> getImage(String imageId, String productId, HttpServletRequest httpServletRequest) {
		User authUser = authHandler.getUser(httpServletRequest);
		if (authUser == null) {
			logger.error("Unauthorized attempt to access image");
			return new ResponseEntity<>(null, HttpStatusCode.valueOf(401));
		}

		if (Utils.isValidNumber(productId) == false) {
			logger.error("Product Id should be a valid integer given is " + productId);
			return new ResponseEntity<>(null, HttpStatusCode.valueOf(400));
		}

		Optional<Product> produOptional = productRepository.findById(Integer.parseInt(productId));

		if (!produOptional.isPresent()) {
			logger.error("Product does not exist with id as " + productId);
			return new ResponseEntity<>(null, HttpStatusCode.valueOf(404));
		}

		if (produOptional.get().getUser().getId() != authUser.getId()) {
			logger.error("Forbidden attempt to access product" + productId + "by user " + authUser.getId());
			return new ResponseEntity<>(null, HttpStatusCode.valueOf(403));
		}
		if (Utils.isValidNumber(imageId) == false) {
			logger.error("imageId should be a valid integer given is " + imageId);
			return new ResponseEntity<>(null, HttpStatusCode.valueOf(400));
		}

		Optional<Image> imageOptional = imageRepository.findById(Integer.parseInt(imageId));

		if (!imageOptional.isPresent()) {
			logger.error("image does not exist with id as " + imageId);
			return new ResponseEntity<>(null, HttpStatusCode.valueOf(404));
		}

		if (imageOptional.get().getProductId() != produOptional.get().getId()) {
			logger.error("Forbidden attempt to access image " + imageId + "of productId  " + productId);
			return new ResponseEntity<>(null, HttpStatusCode.valueOf(403));
		}
		logger.error("image fetched successfully with id " + imageId);
		return new ResponseEntity<>(new ArrayList<>(Arrays.asList(imageOptional.get())), HttpStatusCode.valueOf(200));
	}

	public ResponseEntity<?> deleteImage(String imageId, String productId, HttpServletRequest httpServletRequest) {
		User authUser = authHandler.getUser(httpServletRequest);
		if (authUser == null) {
			logger.error("Unauthorized attempt to access image");
			return new ResponseEntity<>(null, HttpStatusCode.valueOf(401));
		}

		if (Utils.isValidNumber(productId) == false) {
			logger.error("Product Id should be a valid integer given is " + productId);
			return new ResponseEntity<>(null, HttpStatusCode.valueOf(400));
		}

		Optional<Product> produOptional = productRepository.findById(Integer.parseInt(productId));

		if (!produOptional.isPresent()) {
			logger.error("Product does not exist with id as " + productId);
			return new ResponseEntity<>(null, HttpStatusCode.valueOf(404));
		}

		if (produOptional.get().getUser().getId() != authUser.getId()) {
			logger.error("Forbidden attempt to access product" + productId + "by user " + authUser.getId());
			return new ResponseEntity<>(null, HttpStatusCode.valueOf(403));
		}
		if (Utils.isValidNumber(imageId) == false) {
			logger.error("imageId should be a valid integer given is " + imageId);
			return new ResponseEntity<>(null, HttpStatusCode.valueOf(400));
		}

		Optional<Image> imageOptional = imageRepository.findById(Integer.parseInt(imageId));

		if (!imageOptional.isPresent()) {
			logger.error("image does not exist with id as " + imageId);
			return new ResponseEntity<>(null, HttpStatusCode.valueOf(404));
		}

		if (imageOptional.get().getProductId() != produOptional.get().getId()) {
			logger.error("Forbidden attempt to access image " + imageId + "of productId  " + productId);
			return new ResponseEntity<>(null, HttpStatusCode.valueOf(403));
		}
		Image image = imageOptional.get();
		String url = image.getS3BucketPath();
		System.out.println(url);
		String[] urlSplit = url.split("/");
		String bucket = urlSplit[2].split("\\.")[0];
		String filepath = urlSplit[3] + "/" + urlSplit[4];
		s3.deleteObject(bucket, filepath);
		imageRepository.delete(image);
		logger.error("image deleted successfully with id " + imageId);
		return new ResponseEntity<>(null, HttpStatusCode.valueOf(204));
	}
}

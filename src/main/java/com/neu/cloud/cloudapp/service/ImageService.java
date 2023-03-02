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
import org.springframework.beans.factory.annotation.Autowired;
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
import com.neu.cloud.cloudapp.model.Image;
import com.neu.cloud.cloudapp.model.Product;
import com.neu.cloud.cloudapp.model.User;
import com.neu.cloud.cloudapp.repository.ImageRepository;
import com.neu.cloud.cloudapp.repository.ProductRepository;

import jakarta.servlet.http.HttpServletRequest;

@Service
public class ImageService {

	@Autowired
	ImageRepository imageRepository;

	@Autowired
	private ProductRepository productRepository;

	@Autowired
	private AuthHandler authHandler;

	@Autowired
	private AmazonS3 s3;

//	@Value("${aws.s3.bucket}")
	private static final String s3Bucket = "jhdsbxctest-1";

	public ResponseEntity<?> createImage(MultipartFile multipartFile, String productId,
			HttpServletRequest httpServletRequest) {
		try {

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
			String extension = FilenameUtils.getExtension(multipartFile.getOriginalFilename());
			if (!isSupportedExtension(extension)) {
				return new ResponseEntity<>(null, HttpStatusCode.valueOf(400));
			}

			InputStream fis = multipartFile.getInputStream();

			String filename = multipartFile.getOriginalFilename();
			String filepath = UUID.randomUUID() + "/" + filename;
			String bucket = s3Bucket;
			System.out.println(bucket);
			System.out.println("name " + filename);
			System.out.println("name " + extension);

			ObjectMetadata objectMetadata = new ObjectMetadata();
			objectMetadata.setContentLength(multipartFile.getSize());
			objectMetadata.setContentType(multipartFile.getContentType());
			objectMetadata.setCacheControl("public, max-age=31536000");

			try {
				s3.putObject(bucket, filepath, fis, objectMetadata);
			} catch (AmazonServiceException e) {
				return new ResponseEntity<>(null, HttpStatusCode.valueOf(400));
			}

			String imageUrl = String.valueOf(s3.getUrl(bucket, filepath));
			System.out.println(imageUrl);
			Image image = new Image(product.getId(), filename, LocalDateTime.now().toString(), imageUrl);
			imageRepository.save(image);
			return new ResponseEntity<>(convertToImageDto(new ArrayList<>(Arrays.asList(image))),
					HttpStatusCode.valueOf(201));
		} catch (Exception e) {
			return new ResponseEntity<>(null, HttpStatusCode.valueOf(400));
		}
	}

	private boolean isSupportedExtension(String extension) {
		return extension != null && (extension.equals("png") || extension.equals("jpg") || extension.equals("jpeg"));
	}

	public ResponseEntity<?> getAllImage(String productId, HttpServletRequest httpServletRequest) {
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

		List<Image> images = imageRepository.findAllByProductId(Integer.parseInt(productId));

		if (images != null && !images.isEmpty()) {
			return new ResponseEntity<>(convertToImageDto(images), HttpStatusCode.valueOf(200));
		}
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
		if (Utils.isValidNumber(imageId) == false) {
			return new ResponseEntity<>(null, HttpStatusCode.valueOf(400));
		}

		Optional<Image> imageOptional = imageRepository.findById(Integer.parseInt(imageId));

		if (!imageOptional.isPresent()) {
			return new ResponseEntity<>(null, HttpStatusCode.valueOf(404));
		}

		if (imageOptional.get().getProductId() != produOptional.get().getId()) {
			return new ResponseEntity<>(null, HttpStatusCode.valueOf(403));
		}
		return new ResponseEntity<>(new ArrayList<>(Arrays.asList(imageOptional.get())), HttpStatusCode.valueOf(200));
	}

	public ResponseEntity<?> deleteImage(String imageId, String productId, HttpServletRequest httpServletRequest) {
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
		if (Utils.isValidNumber(imageId) == false) {
			return new ResponseEntity<>(null, HttpStatusCode.valueOf(400));
		}

		Optional<Image> imageOptional = imageRepository.findById(Integer.parseInt(imageId));

		if (!imageOptional.isPresent()) {
			return new ResponseEntity<>(null, HttpStatusCode.valueOf(404));
		}

		if (imageOptional.get().getProductId() != produOptional.get().getId()) {
			return new ResponseEntity<>(null, HttpStatusCode.valueOf(403));
		}
		Image image = imageOptional.get();
		String url = image.getS3BucketPath();
		String bucket = url.split("/")[0];
		String filepath = url.split("/")[1] + "/" + url.split("/")[2];
		s3.deleteObject(bucket, filepath);
		imageRepository.delete(image);
		return new ResponseEntity<>(null, HttpStatusCode.valueOf(204));
	}
}

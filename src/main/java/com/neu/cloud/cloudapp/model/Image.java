package com.neu.cloud.cloudapp.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "image")
public class Image {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "image_id")
	private int id;

	@Column(name = "product_id")
	private int productId;

	@Column(name = "file_name")
	private String fileName;

	@Column(name = "date_created")
	private String dateCreated;

	@Column(name = "s3_bucket_path")
	private String s3BucketPath;

	public Image() {
		super();
	}

	public Image(int productId, String fileName, String dateCreated, String s3BucketPath) {
		super();
		this.productId = productId;
		this.fileName = fileName;
		this.dateCreated = dateCreated;
		this.s3BucketPath = s3BucketPath;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getProductId() {
		return productId;
	}

	public void setProductId(int productId) {
		this.productId = productId;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public String getDateCreated() {
		return dateCreated;
	}

	public void setDateCreated(String dateCreated) {
		this.dateCreated = dateCreated;
	}

	public String getS3BucketPath() {
		return s3BucketPath;
	}

	public void setS3BucketPath(String s3BucketPath) {
		this.s3BucketPath = s3BucketPath;
	}

}

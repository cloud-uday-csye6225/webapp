package com.neu.cloud.cloudapp.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.neu.cloud.cloudapp.model.Image;

@Repository
public interface ImageRepository extends JpaRepository<Image, Integer> {

	List<Image> findAllByProductId(int parseInt);


}

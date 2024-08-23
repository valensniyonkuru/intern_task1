package com.boostmytool.beststore.services;

import org.springframework.data.jpa.repository.JpaRepository;

import com.boostmytool.beststore.models.product;

public interface productsRepository extends JpaRepository<product,Integer>{

}

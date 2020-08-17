package com.estafet.boostcd.environment.api.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.estafet.boostcd.environment.api.model.Product;
import com.estafet.boostcd.environment.api.service.ProductService;

@RestController
public class ProductController {

	@Autowired
	private ProductService productService;
	
	@GetMapping("/product/{product}")
	public Product getProduct(@PathVariable String product) {
		return productService.getProduct(product);
	}
	
	@GetMapping("/products")
	public List<Product> getProducts() {
		return productService.getProducts();
	}

	@PostMapping("/product")
	public ResponseEntity<Product> update(@RequestBody Product product) {
		return new ResponseEntity<Product>(productService.update(product), HttpStatus.OK);
	}
	
	@DeleteMapping("/product/{product}")
	public ResponseEntity<Product> deleteProject(@PathVariable String product) {
		return new ResponseEntity<Product>(productService.deleteProduct(product), HttpStatus.OK);
	}

}

package com.estafet.boostcd.environment.api.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.estafet.boostcd.environment.api.model.Product;
import com.estafet.boostcd.environment.api.model.Version;
import com.estafet.boostcd.environment.api.model.VersionComparator;

@Component
public class VersionService {

	@Autowired
	private ProductService productService;

	public Version getVersion() {
		List<String> versions = new ArrayList<String>();
		for (Product product : productService.getProducts()) {
			versions.add(product.getVersion());
		}
		Collections.sort(versions, new VersionComparator());
		Version version = new Version();
		version.setVersion(versions.get(0));
		return version;
	}

}

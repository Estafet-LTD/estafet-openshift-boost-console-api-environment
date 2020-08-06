package com.estafet.boostcd.environment.api.scheduled;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.estafet.boostcd.environment.api.jms.EnvironmentsProducer;
import com.estafet.boostcd.environment.api.model.Product;
import com.estafet.boostcd.environment.api.service.ProductService;

@Component
public class EnvScheduler {

	private static final Logger log = LoggerFactory.getLogger(EnvScheduler.class);
	
	@Autowired
	private ProductService productService;

	@Autowired
	private EnvironmentsProducer environmentsProducer;

	@Scheduled(fixedRate = 30000)
	public void execute() {
		List<Product> products = productService.update();
		if (!products.isEmpty()) {
			for (Product product : products) {
				environmentsProducer.sendMessage(product.getEnvironments());
			}	
		} else {
			log.warn("no updates");
		}
	}

}

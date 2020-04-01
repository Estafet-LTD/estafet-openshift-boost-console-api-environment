package com.estafet.openshift.boost.console.api.environment.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import com.estafet.openshift.boost.console.api.environment.service.MicroserviceService;
import com.estafet.openshift.boost.messages.environments.Environment;
import com.estafet.openshift.boost.messages.environments.EnvironmentApp;

@RestController
public class MicroserviceController {

	@Autowired
	private MicroserviceService microserviceService;

	@GetMapping("/microservices")
	public List<Environment> getMicroserviceEnvironments() {
		return microserviceService.getMicroserviceEnvironments();
	}
	
	@GetMapping("/environment/{env}/app/{app}")
	public Environment getMicroservice(@PathVariable String env, @PathVariable String app) {
		return microserviceService.getMicroservice(env, app);
	}

	@PostMapping("/environment/{env}/app/{app}/{action}")
	public ResponseEntity<Environment> doAction(@PathVariable String env, @PathVariable String app,
			@PathVariable String action) {
		return new ResponseEntity<Environment>(microserviceService.doAction(env, app, action),
				HttpStatus.OK);
	}

}

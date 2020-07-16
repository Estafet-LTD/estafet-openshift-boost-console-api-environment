package com.estafet.boostcd.console.environment.api.controller;

import com.estafet.boostcd.console.environment.api.service.EnvironmentService;
import com.estafet.openshift.boost.messages.environments.Environment;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class EnvironmentController {

	@Autowired
	private EnvironmentService environmentService;

	@PostMapping("/environment/{env}/{action}")
	public ResponseEntity<Environment> doAction(@PathVariable String env,
			@PathVariable String action) {
		return new ResponseEntity<Environment>(environmentService.doAction(env, action),
				HttpStatus.OK);
	}

}

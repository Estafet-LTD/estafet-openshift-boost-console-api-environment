package com.estafet.openshift.boost.console.api.environment.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import com.estafet.openshift.boost.commons.lib.model.API;
import com.estafet.openshift.boost.console.api.environment.model.BuildApp;
import com.estafet.openshift.boost.console.api.environment.model.BuildEnv;
import com.estafet.openshift.boost.console.api.environment.service.BuildEnvService;

@RestController
public class BuildEnvController {

	@Autowired
	private BuildEnvService buildEnvService;

	@Value("${app.version}")
	private String appVersion;

	@GetMapping("/api")
	public API getAPI() {
		return new API(appVersion);
	}

	@GetMapping("/environment")
	public BuildEnv getBuildEnvironment() {
		return buildEnvService.getBuildEnv();
	}

	@PostMapping("/build/app/{app}")
	public ResponseEntity<BuildApp> build(@PathVariable String app) {
		return new ResponseEntity<BuildApp>(buildEnvService.triggerBuildPipeline(app), HttpStatus.OK);
	}

	@PostMapping("/build/apps")
	public ResponseEntity<BuildEnv> buildAll() {
		return new ResponseEntity<BuildEnv>(buildEnvService.triggerBuildAllPipeline(), HttpStatus.OK);
	}

	@PostMapping("/release/app/{app}")
	public ResponseEntity<BuildApp> release(@PathVariable String app) {
		return new ResponseEntity<BuildApp>(buildEnvService.triggerReleasePipeline(app), HttpStatus.OK);
	}

	@PostMapping("/release/apps")
	public ResponseEntity<BuildEnv> releaseAll() {
		return new ResponseEntity<BuildEnv>(buildEnvService.triggerReleaseAllPipeline(), HttpStatus.OK);
	}

}

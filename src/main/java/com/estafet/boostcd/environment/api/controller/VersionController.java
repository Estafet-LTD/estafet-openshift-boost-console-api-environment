package com.estafet.boostcd.environment.api.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.estafet.boostcd.environment.api.model.Version;
import com.estafet.boostcd.environment.api.service.VersionService;

@RestController
public class VersionController {

	@Autowired
	private VersionService versionService;

	@GetMapping("/version")
	public Version getVersion() {
		return versionService.getVersion();
	}

}

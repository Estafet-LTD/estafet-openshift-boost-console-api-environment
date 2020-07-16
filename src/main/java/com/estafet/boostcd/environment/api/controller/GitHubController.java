package com.estafet.boostcd.environment.api.controller;

import com.estafet.boostcd.environment.api.service.GitHubService;
import com.estafet.openshift.boost.messages.github.GitHubHook;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class GitHubController {

	@Autowired
	private GitHubService gitHubService;

	@PostMapping("/github/hooks")
	public ResponseEntity<String> webhook(@RequestBody GitHubHook hook) {
		return new ResponseEntity<String>(gitHubService.webhook(hook), HttpStatus.OK);
	}
	
}

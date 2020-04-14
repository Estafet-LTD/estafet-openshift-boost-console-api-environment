package com.estafet.openshift.boost.console.api.environment.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.estafet.openshift.boost.console.api.environment.service.GitHubService;
import com.estafet.openshift.boost.messages.github.GitHubHook;

@RestController
public class GitHubController {

	@Autowired
	private GitHubService gitHubService;

	@PostMapping("/hooks")
	public ResponseEntity<String> webhook(@RequestBody GitHubHook hook) {
		return new ResponseEntity<String>(gitHubService.webhook(hook), HttpStatus.OK);
	}
	
}

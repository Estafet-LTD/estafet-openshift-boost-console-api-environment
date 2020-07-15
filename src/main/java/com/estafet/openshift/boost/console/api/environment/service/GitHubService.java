package com.estafet.openshift.boost.console.api.environment.service;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.URL;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.estafet.openshift.boost.commons.lib.git.Git;
import com.estafet.openshift.boost.console.api.environment.dao.EnvDAO;
import com.estafet.openshift.boost.console.api.environment.model.Env;
import com.estafet.openshift.boost.console.api.environment.model.Microservice;
import com.estafet.openshift.boost.console.api.environment.model.Microservices;
import com.estafet.openshift.boost.console.api.environment.openshift.BuildConfigParser;
import com.estafet.openshift.boost.console.api.environment.openshift.OpenShiftClient;
import com.estafet.openshift.boost.messages.github.GitHubHook;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.openshift.restclient.model.IBuildConfig;

@Service
public class GitHubService {

	@Autowired
	private OpenShiftClient client;

	@Autowired
	private EnvDAO envDAO;

	public String webhook(GitHubHook hook) {
		if (hook.getHook() != null) {
			return "ping_success";
		} else {
			for (IBuildConfig buildConfig : client.getBuildConfigs()) {
				if (compareURL(hook, buildConfig)) {
					client.executeBuildPipeline(buildConfig.getName());
					return "build_success";
				}
			}
			for (Env env : envDAO.getEnvs()) {
				if (!env.getName().equals("build")) {
					IBuildConfig buildConfig = client.getTestBuildConfig(env.getName());
					if (compareURL(hook, buildConfig)) {
						client.executeTestPipeline(env.getName());
						return "test_success";
					}
				}
			}
			String app = getNewApp(hook);
			if (app != null) {
				client.executeBuildPipeline(app, hook.getRepository().getHtmlUrl());
				return "build_success";
			}
			return "no_pipline_triggered";
		}
	}
	
	private String getNewApp(GitHubHook hook) {
		Git git = new Git(System.getenv("PRODUCT_REPO"));
		String url = "https://raw.githubusercontent.com/" + git.uri() + "/" + git.org() + "/master/setup-environments/vars/microservices-vars.yml";
		BufferedInputStream in = null;
		try {
			in = new BufferedInputStream(new URL(url).openStream());
			ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
			Microservices microservices = mapper.readValue(in, Microservices.class);
			for (Microservice microservice : microservices.getMicroservices()) {
				if (microservice.getRepo().equals(hook.getRepository().getName())) {
					return microservice.getName();
				}
			}
			return null;
		} catch (IOException e) {
			throw new RuntimeException(e);
		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			}
		}
	}

	private boolean compareURL(GitHubHook hook, IBuildConfig buildConfig) {
		return hook.getRef().equals("refs/heads/master") && new BuildConfigParser(buildConfig).getGitRepository()
				.equalsIgnoreCase(hook.getRepository().getSvnUrl());
	}

}

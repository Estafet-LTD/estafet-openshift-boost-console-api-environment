package com.estafet.boostcd.environment.api.service;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.URL;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.estafet.boostcd.commons.git.Git;
import com.estafet.boostcd.environment.api.dao.ProductDAO;
import com.estafet.boostcd.environment.api.model.Env;
import com.estafet.boostcd.environment.api.model.Microservice;
import com.estafet.boostcd.environment.api.model.Microservices;
import com.estafet.boostcd.environment.api.model.Product;
import com.estafet.boostcd.openshift.BuildConfigParser;
import com.estafet.boostcd.openshift.OpenShiftClient;
import com.estafet.openshift.boost.messages.github.GitHubHook;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.openshift.restclient.model.IBuildConfig;

@Service
public class GitHubService {

	@Autowired
	private OpenShiftClient client;

	@Autowired
	private ProductDAO productDAO;

	public String webhook(GitHubHook hook) {
		if (hook.getHook() != null) {
			return "ping_success";
		} else {
			for (Product product : productDAO.getProducts()) {
				for (IBuildConfig buildConfig : client.getBuildConfigs(product.getProductId())) {
					if (compareURL(hook, buildConfig)) {
						client.executeBuildPipeline(product.getProductId(), product.getRepo(), buildConfig.getName());
						return "build_success";
					}
				}
			}
			for (Product product : productDAO.getProducts()) {
				for (Env env : product.getEnvs()) {
					if (!env.getName().equals("build")) {

						IBuildConfig buildConfig = client.getTestBuildConfig(product.getProductId(), env.getName());
						if (compareURL(hook, buildConfig)) {
							client.executeTestPipeline(product.getProductId(), product.getRepo(), env.getName());
							return "test_success";
						}
					}
				}

			}
			for (Product product : productDAO.getProducts()) {
				client.executeBuildPipeline(product.getProductId(), product.getRepo(),
						getNewApp(hook, product.getRepo()), hook.getRepository().getHtmlUrl());
				return "build_success";
			}
			return "no_pipline_triggered";
		}
	}

	private String getNewApp(GitHubHook hook, String productRepo) {
		Git git = new Git(productRepo);
		String url = "https://raw.githubusercontent.com/" + git.org() + "/" + git.uri()
				+ "/master/src/boost/openshift/definitions/microservices.yml";
		BufferedInputStream in = null;
		try {
			in = new BufferedInputStream(new URL(url).openStream());
			ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
			Microservices microservices = mapper.readValue(in, Microservices.class);
			for (Microservice microservice : microservices.getMicroservices()) {
				if (microservice.getRepo().replaceAll("\\.git", "")
						.equals(hook.getRepository().getUrl())) {
					return microservice.getName();
				}
			}
			throw new RuntimeException("Cannot match app for repo - " + hook.getRepository().getUrl());
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

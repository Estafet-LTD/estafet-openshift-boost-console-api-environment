package com.estafet.openshift.boost.console.api.environment.model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.estafet.openshift.boost.commons.lib.model.API;
import com.estafet.openshift.boost.console.api.environment.openshift.DeploymentConfigParser;
import com.estafet.openshift.boost.console.api.environment.openshift.ImageStreamParser;
import com.openshift.restclient.model.IDeploymentConfig;
import com.openshift.restclient.model.IImageStream;
import com.openshift.restclient.model.IService;

@Component
public class AppFactory {

	private static final Logger log = LoggerFactory.getLogger(AppFactory.class);
	
	public App getBuildApp(IDeploymentConfig dc, IService service, 
			IImageStream buildImage, IImageStream cicdImage) {
		if (dc == null || service == null) {
			return null;
		} else {
			return App.builder()
					.setDeployed(isDeployed(service))
					.setDeployedDate(new DeploymentConfigParser(dc).getDeployedDate())
					.setName(appName(dc))
					.setVersion(getBuildVersion(buildImage, cicdImage))
					.build();
		}
	}
	
	public App getApp(IDeploymentConfig dc, IService service) {
		log.debug("app - " + dc.getName());
		if (dc == null || service == null) {
			return null;
		} else {
			return App.builder()
					.setDeployed(isDeployed(service))
					.setDeployedDate(new DeploymentConfigParser(dc).getDeployedDate())
					.setName(appName(dc))
					.setVersion(new DeploymentConfigParser(dc).getVersion())
					.build();
		}
	}

	public String appName(IDeploymentConfig dc) {
		String name = dc.getName();
		if (name.startsWith("green")) {
			return name.substring("green".length());
		} else if (name.startsWith("blue")) {
			return name.substring("blue".length());
		} else {
			return name;
		}
	}

	private String getBuildVersion(IImageStream buildImage, IImageStream cicdImage) {
		String sha = new ImageStreamParser(buildImage).getLatestTag();
		return new ImageStreamParser(cicdImage).getTagBySha(sha);
	}

	private boolean isDeployed(IService service) {
		try {
			new RestTemplate().getForObject(createURL(service), API.class).getVersion();
			return true;
		} catch (RestClientException e) {
			return false;
		}
	}

	private String createURL(IService service) {
		return "http://" + service.getName() + "." + service.getNamespaceName() + ".svc" + ":8080/api";
	}

}

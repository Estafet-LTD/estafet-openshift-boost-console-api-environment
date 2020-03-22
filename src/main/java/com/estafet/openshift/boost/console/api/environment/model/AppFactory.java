package com.estafet.openshift.boost.console.api.environment.model;

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

	public App getBuildApp(IDeploymentConfig dc, IService service, 
			IImageStream buildImage, IImageStream cicdImage) {
		if (dc == null & service == null) {
			return null;
		} else {
			return App.builder()
					.setDeployed(isDeployed(service))
					.setDeployedDate(new DeploymentConfigParser(dc).getDeployedDate())
					.setName(dc.getName())
					.setVersion(getBuildVersion(buildImage, cicdImage))
					.build();
		}
	}
	
	public App getApp(IDeploymentConfig dc, IService service) {
		if (dc == null & service == null) {
			return null;
		} else {
			return App.builder()
					.setDeployed(isDeployed(service))
					.setDeployedDate(new DeploymentConfigParser(dc).getDeployedDate())
					.setName(dc.getName())
					.setVersion(new DeploymentConfigParser(dc).getVersion())
					.build();
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

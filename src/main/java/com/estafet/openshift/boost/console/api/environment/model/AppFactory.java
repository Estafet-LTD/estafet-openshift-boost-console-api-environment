package com.estafet.openshift.boost.console.api.environment.model;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.estafet.openshift.boost.commons.lib.model.API;
import com.estafet.openshift.boost.commons.lib.openshift.NamespaceUtils;
import com.estafet.openshift.boost.console.api.environment.openshift.DeploymentConfigParser;
import com.estafet.openshift.boost.console.api.environment.openshift.ImageStreamParser;
import com.estafet.openshift.boost.console.api.environment.openshift.OpenShiftClient;
import com.openshift.restclient.model.IDeploymentConfig;
import com.openshift.restclient.model.IImageStream;
import com.openshift.restclient.model.IService;

@Component
public class AppFactory {

	@Autowired
	private OpenShiftClient client;

	public App create(String namespace, IService service) {

		IDeploymentConfig deploymentConfig;

		deploymentConfig = client.getDeploymentConfig(namespace, service.getName());
		

		App app = new App();
		app.setName(service.getName());
		app.setDeployed(isDeployed(service));
		app.setTestStatus(deploymentConfig.getLabels().get("testStatus"));

		try {
			app.setVersion(getVersion(namespace, deploymentConfig, service.getName()));
		} catch (RuntimeException e) {
			app.setErrors(e.getMessage());
			app.setVersion("0.0.0");
		}

		try {
			app.setDeployedDate(new DeploymentConfigParser(deploymentConfig).getDeployedDate());
		} catch (RuntimeException e) {
			app.setErrors(e.getMessage());
		}

		return app;
	}

	private String getVersion(String namespace, IDeploymentConfig deploymentConfig, String name) {
		if (NamespaceUtils.namespace("build").equals(namespace)) {
			IImageStream buildImageStream = client.getImageStream(NamespaceUtils.namespace("build"), name);
			String sha = new ImageStreamParser(buildImageStream).getLatestTag();
			IImageStream cicdImageStream = client.getImageStream(NamespaceUtils.namespace("cicd"), name);
			return new ImageStreamParser(cicdImageStream).getTagBySha(sha);
		} else {
			return new DeploymentConfigParser(deploymentConfig).getVersion();
		}
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

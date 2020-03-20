package com.estafet.openshift.boost.console.api.environment.model;

import java.util.ArrayList;
import java.util.List;

import org.springframework.web.client.RestTemplate;

import com.estafet.openshift.boost.commons.lib.date.DateUtils;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.openshift.restclient.NotFoundException;

public class BuildEnvBuilder {

	private RestTemplate restTemplate;

	private List<BuildApp> buildApps = new ArrayList<BuildApp>();

	public BuildEnvBuilder addBuildApp(BuildApp buildApp) {
		buildApps.add(buildApp);
		return this;
	}

	public BuildEnvBuilder setRestTemplate(RestTemplate restTemplate) {
		this.restTemplate = restTemplate;
		return this;
	}

	public BuildEnv build() {
		BuildEnv buildEnv = new BuildEnv();
		buildEnv.setName("build");
		buildEnv.setUpdatedDate(DateUtils.newDate());
		if (!buildApps.isEmpty()) {
			for (BuildApp buildApp : buildApps) {
				buildEnv.addApp(buildApp);
			}
		} else {
			for (App app : getApps()) {
				buildEnv.addApp(createBuildApp(app));
			}
		}
		return buildEnv;
	}

	@SuppressWarnings("rawtypes")
	private List<App> getApps() {
		List objects = restTemplate.getForObject(
				"http://console-app-api." + getProduct() + "-monitoring.svc:8080/environment/build/apps", List.class);
		List<App> apps = new ArrayList<App>();
		ObjectMapper mapper = new ObjectMapper();
		for (Object object : objects) {
			App app = mapper.convertValue(object, new TypeReference<App>() {
			});
			apps.add(app);
		}
		return apps;
	}

	private String getProduct() {
		return System.getenv("PRODUCT");
	}

	private BuildApp createBuildApp(App app) {
		try {
			return new BuildAppBuilder()
					.setApp(app)
					.build();
		} catch (NotFoundException e) {
			return new BuildAppBuilder()
					.setVersion("0.0.0")
					.setName(app.getName())
					.setCanRelease(false)
					.setDeployed(false)
					.setErrors(e.getMessage())
					.build();
		}
	}

}

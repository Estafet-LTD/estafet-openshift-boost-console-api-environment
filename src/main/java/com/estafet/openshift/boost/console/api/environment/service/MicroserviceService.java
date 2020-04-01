package com.estafet.openshift.boost.console.api.environment.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.estafet.openshift.boost.console.api.environment.dao.EnvDAO;
import com.estafet.openshift.boost.console.api.environment.model.App;
import com.estafet.openshift.boost.console.api.environment.model.Env;
import com.estafet.openshift.boost.console.api.environment.openshift.OpenShiftClient;
import com.estafet.openshift.boost.messages.environments.Environment;
import com.estafet.openshift.boost.messages.environments.EnvironmentApp;

@Service
public class MicroserviceService {

	@Autowired
	private OpenShiftClient client;
	
	@Autowired
	private EnvDAO envDAO;

	@Transactional(readOnly = true)
	public List<Environment> getMicroserviceEnvironments() {
		return getMicroserviceEnvironments("build");
	}
	
	public List<Environment> getMicroserviceEnvironments(String envId) {
		return getMicroserviceEnvironments(envId, new ArrayList<Environment>());
	}
	
	private List<Environment> getMicroserviceEnvironments(String envId, List<Environment> envs) {
		Env env = envDAO.getEnv(envId);
		envs.add(env.getEnvironment());
		if (!env.getNext().equals("prod")) {
			return getMicroserviceEnvironments(env.getNext(), envs);
		} else {
			Env green = envDAO.getEnv("green");
			Env blue = envDAO.getEnv("blue");
			if (green.getLive()) {
				envs.add(blue.getEnvironment());
				envs.add(green.getEnvironment());
			} else {
				envs.add(green.getEnvironment());
				envs.add(blue.getEnvironment());
			}
			return envs;
		}
	}
	
	public EnvironmentApp getMicroservice(String envId, String appId) {
	List<App> envApps = envDAO.getEnv(envId).getApps();
	EnvironmentApp ennvironmentApp = new EnvironmentApp(); 
	for(App app : envApps) {
		if (app.getName().equals(appId)) {
			ennvironmentApp = app.getEnvironmentApp();
			break;
		}
	}
	return ennvironmentApp;
}

	public Environment doAction(String env, String app, String action) {
		if (env.equals("build")) {
			if (action.equals("build")) {
				client.executeBuildPipeline(app);
			} else if (action.equals("promote")) {
				client.executeReleasePipeline(app);
			}
		} else {
			if (action.equals("promote")) {
				client.executePromotePipeline(env, app);
			} 
		}
		return envDAO.getEnv(env).getEnvironment();
	}
	
}

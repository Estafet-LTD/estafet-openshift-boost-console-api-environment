package com.estafet.boostcd.environment.api.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.estafet.boostcd.environment.api.dao.EnvDAO;
import com.estafet.boostcd.environment.api.model.App;
import com.estafet.boostcd.environment.api.model.Env;
import com.estafet.boostcd.openshift.OpenShiftClient;
import com.estafet.openshift.boost.messages.environments.Environment;

@Service
public class MicroserviceService {

	@Autowired
	private OpenShiftClient client;
	
	@Autowired
	private EnvDAO envDAO;

	@Transactional(readOnly = true)
	public List<Environment> getMicroserviceEnvironments(String productId) {
		return getMicroserviceEnvironments(productId, "build");
	}
	
	public List<Environment> getMicroserviceEnvironments(String productId, String envId) {
		return getMicroserviceEnvironments(productId, envId, new ArrayList<Environment>());
	}
	
	private List<Environment> getMicroserviceEnvironments(String productId, String envId, List<Environment> envs) {
		Env env = envDAO.getEnv(productId, envId);
		envs.add(env.getEnvironment());
		if (!env.getNext().equals("prod")) {
			return getMicroserviceEnvironments(productId, env.getNext(), envs);
		} else {
			Env green = envDAO.getEnv(productId, "green");
			Env blue = envDAO.getEnv(productId, "blue");
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
	
	public Environment getMicroservice(String productId, String envId, String appId) {
	Environment environment = new Environment();
	Env env = envDAO.getEnv(productId, envId);	
	List<App> envApps = env.getApps();
	for(App app : envApps) {
		if (app.getName().equals(appId)) {
			envApps.clear();
			envApps.add(app);
			env.setApps(envApps);
			environment = env.getEnvironment();
			break;
		}
	}
	return environment;
}

	public Environment doAction(String productId, String env, String app, String action) {
		if (env.equals("build")) {
			if (action.equals("build")) {
				client.executeBuildPipeline(productId, app);
			} else if (action.equals("promote")) {
				client.executeReleasePipeline(productId, app);
			}
		} else {
			if (action.equals("promote")) {
				client.executePromotePipeline(productId, env, app, envDAO.getEnv(productId, env).getNext());
			} 
		}
		return envDAO.getEnv(productId, env).getEnvironment();
	}
	
}

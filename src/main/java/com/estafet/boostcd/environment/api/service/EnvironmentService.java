package com.estafet.boostcd.environment.api.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.estafet.boostcd.environment.api.dao.EnvDAO;
import com.estafet.boostcd.openshift.OpenShiftClient;
import com.estafet.openshift.boost.messages.environments.Environment;

@Service
public class EnvironmentService {

	static final Logger log = LoggerFactory.getLogger(EnvironmentService.class);
	
	@Autowired
	private OpenShiftClient client;
	
	@Autowired EnvDAO envDAO;
	
	public Environment doAction(String productId, String env, String action) {
		if (env.equals("build")) {
			if (action.equals("build")) {
				client.executeBuildAllPipeline(productId);
			} else if (action.equals("promote")) {
				client.executeReleaseAllPipeline(productId);
			}
		} else {
			if (action.equals("promote")) {
				client.executePromoteAllPipeline(productId, env, envDAO.getEnv(productId, env).getNext());
			} else if (action.equals("test")) {
				client.executeTestPipeline(productId, env);
			} else if (action.equals("go-live") || action.equals("back-out")) {
				client.executePromoteToLivePipeline(productId);
			}
		}
		return envDAO.getEnv(productId, env).getEnvironment();
	}

}

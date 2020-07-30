package com.estafet.boostcd.environment.api.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.estafet.boostcd.environment.api.dao.EnvDAO;
import com.estafet.boostcd.environment.api.dao.ProductDAO;
import com.estafet.boostcd.openshift.OpenShiftClient;
import com.estafet.openshift.boost.messages.environments.Environment;

@Service
public class EnvironmentService {

	@Autowired
	private OpenShiftClient client;
	
	@Autowired 
	private EnvDAO envDAO;
	
	@Autowired
	private ProductDAO productDAO;
	
	public Environment doAction(String productId, String env, String action) {
		String productRepo = productDAO.getProduct(productId).getRepo();
		if (env.equals("build")) {
			if (action.equals("build")) {
				client.executeBuildAllPipeline(productId, productRepo);
			} else if (action.equals("promote")) {
				client.executeReleaseAllPipeline(productId, productRepo);
			}
		} else {
			if (action.equals("promote")) {
				client.executePromoteAllPipeline(productId, productRepo, env, envDAO.getEnv(productId, env).getNext());
			} else if (action.equals("test")) {
				client.executeTestPipeline(productId, productRepo, env);
			} else if (action.equals("go-live") || action.equals("back-out")) {
				client.executePromoteToLivePipeline(productId);
			}
		}
		return envDAO.getEnv(productId, env).getEnvironment();
	}

}

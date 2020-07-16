package com.estafet.boostcd.console.environment.api.service;

import java.util.ArrayList;
import java.util.List;

import com.estafet.boostcd.console.environment.api.dao.EnvDAO;
import com.estafet.boostcd.console.environment.api.model.Env;
import com.estafet.boostcd.console.environment.api.model.EnvFactory;
import com.estafet.boostcd.console.environment.api.openshift.OpenShiftClient;
import com.estafet.openshift.boost.messages.environments.Environment;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EnvironmentService {

	private static final Logger log = LoggerFactory.getLogger(EnvironmentService.class);
	
	@Autowired
	private OpenShiftClient client;
	
	@Autowired
	private EnvDAO envDAO;
	
	@Autowired
	private EnvFactory envFactory;
	
	public Environment doAction(String env, String action) {
		if (env.equals("build")) {
			if (action.equals("build")) {
				client.executeBuildAllPipeline();
			} else if (action.equals("promote")) {
				client.executeReleaseAllPipeline();
			}
		} else {
			if (action.equals("promote")) {
				client.executePromoteAllPipeline(env);
			} else if (action.equals("test")) {
				client.executeTestPipeline(env);
			} else if (action.equals("go-live") || action.equals("back-out")) {
				client.executePromoteToLivePipeline();
			}
		}
		return envDAO.getEnv(env).getEnvironment();
	}
	
	@Transactional
	public List<Env> updateEnvs() {
		List<Env> result = new ArrayList<Env>();
		for (Env env : envFactory.getEnvs()) {
			log.debug("scanned - " + env.toString());
			Env savedEnv = envDAO.getEnv(env.getName());
			if (savedEnv == null) {
				savedEnv = envDAO.createEnv(env);
			} else if (savedEnv.changed(env)) {
				savedEnv = envDAO.updateEnv(savedEnv.merge(env));
			}
			result.add(savedEnv);
		}
		return result;
	}

}

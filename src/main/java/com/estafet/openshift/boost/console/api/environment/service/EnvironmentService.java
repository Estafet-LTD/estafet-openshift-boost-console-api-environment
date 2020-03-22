package com.estafet.openshift.boost.console.api.environment.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.estafet.openshift.boost.console.api.environment.dao.EnvDAO;
import com.estafet.openshift.boost.console.api.environment.model.App;
import com.estafet.openshift.boost.console.api.environment.model.AppFactory;
import com.estafet.openshift.boost.console.api.environment.model.Env;
import com.estafet.openshift.boost.console.api.environment.openshift.OpenShiftClient;
import com.estafet.openshift.boost.console.api.environment.util.ENV;
import com.estafet.openshift.boost.messages.environments.Environment;
import com.openshift.restclient.model.IBuild;
import com.openshift.restclient.model.IDeploymentConfig;
import com.openshift.restclient.model.IImageStream;
import com.openshift.restclient.model.IProject;
import com.openshift.restclient.model.IService;

@Service
public class EnvironmentService {

	private static final Logger log = LoggerFactory.getLogger(EnvironmentService.class);
	
	@Autowired
	private OpenShiftClient client;
	
	@Autowired
	private EnvDAO envDAO;

	@Autowired
	private AppFactory appFactory;
	
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
		for (Env env : createEnv(ENV.PRODUCT + "-build", client.getProjects())) {
			Env savedEnv = envDAO.getEnv(env.getName());
			if (savedEnv == null) {
				result.add(env);
			} else {
				result.add(savedEnv.update(env));
			}
		}
		return result;
	}
	
	private List<Env> createEnv(String namespace, Map<String, IProject> projects) {
		return createEnv(namespace, projects, new ArrayList<Env>());
	}
	
	private List<Env> createEnv(String namespace, Map<String, IProject> projects, List<Env> envs) {
		IProject project = projects.get(namespace);
		String next = project.getLabels().get("next");
		envs.add(createEnv(project, next));
		if (!next.equals("end")) {
			return createEnv(next, projects, envs);
		} else if (namespace.equals(ENV.PRODUCT + "-prod")) {
			envs.add(createProdEnv("green"));
			envs.add(createProdEnv("blue"));
			return envs;
		}
		throw new RuntimeException("cannot create Env for namespace - " + namespace);
	}

	public Env createProdEnv(String name) {
		Env env =  Env.builder()
					.setName(name)
					.setLive(isLive(name))
					.setTested(client.isEnvironmentTestPassed(ENV.PRODUCT + "-prod"))
					.build();
		return addApps(env, ENV.PRODUCT + "-prod");
	}

	public Env createEnv(IProject project, String next) {
		Env env = Env.builder()
					.setName(envName(project))
					.setDisplayName(project.getDisplayName())
					.setNext(next)
					.setTested(client.isEnvironmentTestPassed(project.getName()))
					.build();
		return addApps(env, project.getName());
	}

	public Env addApps(Env env, String namespace) {
		Map<String, IDeploymentConfig> dcs = client.getDeploymentConfigs(namespace);
		Map<String, IService> services = client.getServices(namespace);
		Map<String, IImageStream> buildImages = client.getImageStreams(ENV.PRODUCT + "-build");
		Map<String, IImageStream> cicdImages = client.getImageStreams(ENV.PRODUCT + "-cicd");
		for (IBuild build : client.getBuilds()) {
			String appName = appName(build);
			try {
				App app;
				if (env.getName().equals("build")) {
					app = appFactory.getBuildApp(dcs.get(appName), services.get(appName), buildImages.get(appName), cicdImages.get(appName));	
				} else {
					app = appFactory.getApp(dcs.get(appName), services.get(appName));
				}
				if (app != null) {
					env.addApp(app);	
				}
			} catch (RuntimeException e) {
				log.warn("There was a problem when constructing app - " + appName, e);
			}
		}
		return env;
	}
	
	private boolean isLive(String name) {
		return client.getRoute().getServiceName().startsWith(name);
	}
	
	private String appName(IBuild build) {
		return build.getName().replaceAll("build\\-", "");
	}
	
	private String envName(IProject project) {
		return project.getName().replaceAll(Pattern.quote(ENV.PRODUCT) + "\\-", "");
	}

}

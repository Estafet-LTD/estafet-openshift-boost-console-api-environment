package com.estafet.boostcd.console.environment.api.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import com.estafet.boostcd.console.environment.api.openshift.OpenShiftClient;
import com.estafet.openshift.boost.commons.lib.env.ENV;
import com.openshift.restclient.model.IDeploymentConfig;
import com.openshift.restclient.model.IImageStream;
import com.openshift.restclient.model.IProject;
import com.openshift.restclient.model.IService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class EnvFactory {

	private static final Logger log = LoggerFactory.getLogger(EnvFactory.class);
	
	@Autowired
	private OpenShiftClient client;
	
	@Autowired
	private AppFactory appFactory;
	
	public List<Env> getEnvs() {
		return getEnvs(ENV.BUILD, client.getProjects());
	}
	
	private List<Env> getEnvs(String namespace, Map<String, IProject> projects) {
		return getEnvs(namespace, projects, new ArrayList<Env>());
	}
	
	private List<Env> getEnvs(String namespace, Map<String, IProject> projects, List<Env> envs) {
		IProject project = projects.get(namespace);
		String next = project.getLabels().get("next");
		if (!next.equals(ENV.PRODUCT + "-end")) {
			envs.add(createEnv(project, next));
			return getEnvs(next, projects, envs);
		} else if (namespace.equals(ENV.PROD)) {
			envs.add(createProdEnv("green", project));
			envs.add(createProdEnv("blue", project));
			return envs;
		}
		throw new RuntimeException("cannot create Env for namespace - " + namespace);
	}

	private Env createProdEnv(String name, IProject project) {
		log.debug("createProEnv - " + name);
		Env env =  Env.builder()
					.setName(name)
					.setDisplayName(isLive(name) ? "Live" : "Staging")
					.setLive(isLive(name))
					.setTested(prodTestedStatus(name, project))
					.build();
		return addApps(env, ENV.PROD);
	}

	public Boolean prodTestedStatus(String name, IProject project) {
		if (isLive(name)) {
			return null;
		} else {
			return client.isEnvironmentTestPassed(project);	
		}
	}

	private Env createEnv(IProject project, String next) {
		log.debug("createEnv - " + project.getName());
		Env env = Env.builder()
					.setName(envName(project.getName()))
					.setDisplayName(project.getLabels().get("display"))
					.setNext(envName(next))
					.setTested(testedStatus(project))
					.build();
		return addApps(env, project.getName());
	}

	public Boolean testedStatus(IProject project) {
		if (project.getName().equals(ENV.BUILD)) {
			return null;
		} 
		return client.isEnvironmentTestPassed(project);
	}

	private Env addApps(Env env, String namespace) {
		Map<String, IDeploymentConfig> dcs = client.getDeploymentConfigs(namespace);
		Map<String, IService> services = client.getServices(namespace);
		for (String appName : dcs.keySet()) {
			try {
				App app = null;
				IDeploymentConfig dc = dcs.get(appName);
				IService service = services.get(appName);
				if (env.getName().equals("build")) {
					Map<String, IImageStream> cicdImages = client.getCICDImageStreams();
					Map<String, IImageStream> images = client.getImageStreams(namespace);
					app = appFactory.getBuildApp(dc, service, images.get(appName), cicdImages.get(appName));	
				} else {
					app = appFactory.getApp(dc, service);
				}
				if (app != null) {
					if ((env.isProd() && appName.startsWith(env.getName())) || !env.isProd()) {
						env.addApp(app);	
					}
				} else {
					log.warn("could not construct app for - " + appName);
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
	
	private String envName(String namespace) {
		return namespace.replaceAll(Pattern.quote(ENV.PRODUCT) + "\\-", "");
	}
	
}

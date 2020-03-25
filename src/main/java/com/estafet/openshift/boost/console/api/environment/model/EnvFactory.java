package com.estafet.openshift.boost.console.api.environment.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.estafet.openshift.boost.commons.lib.env.ENV;
import com.estafet.openshift.boost.console.api.environment.openshift.OpenShiftClient;
import com.openshift.restclient.model.IDeploymentConfig;
import com.openshift.restclient.model.IImageStream;
import com.openshift.restclient.model.IProject;
import com.openshift.restclient.model.IService;

@Component
public class EnvFactory {

	private static final Logger log = LoggerFactory.getLogger(EnvFactory.class);
	
	@Autowired
	private OpenShiftClient client;
	
	@Autowired
	private AppFactory appFactory;
	
	public List<Env> getEnvs(String namespace) {
		return getEnvs(namespace, client.getProjects());
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
			envs.add(createProdEnv("green"));
			envs.add(createProdEnv("blue"));
			return envs;
		}
		throw new RuntimeException("cannot create Env for namespace - " + namespace);
	}

	public Env createProdEnv(String name) {
		log.info("createProEnv - " + name);
		Env env =  Env.builder()
					.setName(name)
					.setDisplayName(isLive(name) ? "Live" : "Staging")
					.setLive(isLive(name))
					.setTested(client.isEnvironmentTestPassed(ENV.PROD))
					.build();
		return addApps(env, ENV.PROD);
	}

	public Env createEnv(IProject project, String next) {
		log.info("createEnv - " + project.getName());
		Env env = Env.builder()
					.setName(envName(project.getName()))
					.setDisplayName(project.getLabels().get("display"))
					.setNext(envName(next))
					.setTested(client.isEnvironmentTestPassed(project.getName()))
					.build();
		return addApps(env, project.getName());
	}

	public Env addApps(Env env, String namespace) {
		Map<String, IDeploymentConfig> dcs = client.getDeploymentConfigs(namespace);
		Map<String, IService> services = client.getServices(namespace);
		Map<String, IImageStream> images = client.getImageStreams(namespace);
		Map<String, IImageStream> cicdImages = client.getCICDImageStreams();
		for (String appName : dcs.keySet()) {
			try {
				App app = null;
				IDeploymentConfig dc = dcs.get(appName);
				IService service = services.get(appName);
				if (env.getName().equals("build")) {
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
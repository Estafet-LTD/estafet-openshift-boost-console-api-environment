package com.estafet.openshift.boost.console.api.environment.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.estafet.openshift.boost.console.api.environment.dao.EnvDAO;
import com.estafet.openshift.boost.console.api.environment.model.Env;
import com.estafet.openshift.boost.console.api.environment.openshift.BuildConfigParser;
import com.estafet.openshift.boost.console.api.environment.openshift.OpenShiftClient;
import com.estafet.openshift.boost.messages.github.GitHubHook;
import com.openshift.restclient.model.IBuildConfig;

@Service
public class GitHubService {
	
	@Autowired
	private OpenShiftClient client;
	
	@Autowired
	private EnvDAO envDAO;

	public String webhook(GitHubHook hook) {
		if (hook.getHook() != null) {
			return "ping_success";
		} else {
			String url = hook.getRepository().getSvnUrl();
			for (IBuildConfig buildConfig : client.getBuildConfigs()) {
				if (compareURL(url, buildConfig)) {
					client.executeBuildPipeline(buildConfig.getName());
					return "build_success";
				}
			}
			for (Env env : envDAO.getEnvs()) {
				if (!env.getName().equals("build")) {
					IBuildConfig buildConfig = client.getTestBuildConfig(env.getName());
					if (compareURL(url, buildConfig)) {
						client.executeTestPipeline(env.getName());
						return "test_success";
					}
				}
			}
			throw new RuntimeException("Cannot find buildconfig for webhook");
		}
	}

	private boolean compareURL(String url, IBuildConfig buildConfig) {
		return new BuildConfigParser(buildConfig).getGitRepository().equalsIgnoreCase(url);
	}

}

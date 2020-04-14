package com.estafet.openshift.boost.console.api.environment.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.estafet.openshift.boost.console.api.environment.openshift.BuildConfigParser;
import com.estafet.openshift.boost.console.api.environment.openshift.OpenShiftClient;
import com.estafet.openshift.boost.messages.github.GitHubHook;
import com.openshift.restclient.model.IBuildConfig;

@Service
public class GitHubService {
	
	@Autowired
	private OpenShiftClient client;

	public String webhook(GitHubHook hook) {
		if (hook.getHook() != null) {
			return "success";
		} else {
			String url = hook.getRepository().getCloneUrl();
			for (IBuildConfig buildConfig : client.getBuildConfigs()) {
				if (new BuildConfigParser(buildConfig).getGitRepository().equalsIgnoreCase(url)) {
					client.executeBuildPipeline(buildConfig.getName());
					return "success";
				}
			}
			throw new RuntimeException("Cannot find buildconfig for webhook");
		}
	}

}

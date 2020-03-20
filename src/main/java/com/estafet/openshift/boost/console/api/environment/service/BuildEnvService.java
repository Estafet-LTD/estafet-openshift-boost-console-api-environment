package com.estafet.openshift.boost.console.api.environment.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.estafet.openshift.boost.console.api.build.dao.BuildEnvDAO;
import com.estafet.openshift.boost.console.api.environment.model.BuildApp;
import com.estafet.openshift.boost.console.api.environment.model.BuildEnv;
import com.estafet.openshift.boost.console.api.environment.openshift.OpenShiftClient;
import com.openshift.restclient.capability.CapabilityVisitor;
import com.openshift.restclient.capability.resources.IBuildTriggerable;
import com.openshift.restclient.model.IBuild;
import com.openshift.restclient.model.IBuildConfig;

@Service
public class BuildEnvService {

	@Autowired
	private OpenShiftClient client;
	
	@Autowired
	private BuildEnvDAO buildEnvDAO;

	@Transactional(readOnly = true)
	public BuildEnv getBuildEnv() {
		return buildEnvDAO.getBuildEnv();
	}

	@Transactional(readOnly = true)
	public BuildApp triggerReleasePipeline(String app) {
		executePipeline(client.getReleasePipeline(app));
		return buildEnvDAO.getBuildApp(app);
	}
	
	@Transactional(readOnly = true)
	public BuildEnv triggerReleaseAllPipeline() {
		executePipeline(client.getReleaseAllPipeline());
		return buildEnvDAO.getBuildEnv();
	}
	
	@Transactional(readOnly = true)
	public BuildApp triggerBuildPipeline(String app) {
		executePipeline(client.getBuildPipeline(app));
		return buildEnvDAO.getBuildApp(app);
	}
	
	@Transactional(readOnly = true)
	public BuildEnv triggerBuildAllPipeline() {
		executePipeline(client.getBuildAllPipeline());
		return buildEnvDAO.getBuildEnv();
	}

	private void executePipeline(IBuildConfig pipeline) {
		pipeline.accept(new CapabilityVisitor<IBuildTriggerable, IBuild>() {
            @Override
            public IBuild visit(IBuildTriggerable capability) {
                return capability.trigger();
            }
        }, null);
	}
}

package com.estafet.openshift.boost.console.api.environment.model;

import com.estafet.openshift.boost.commons.lib.date.DateUtils;

public class BuildAppBuilder {

	private String name;

	private String version;

	private boolean canRelease;

	private String deployedDate;

	private String errors;

	private boolean deployed;
	
	private App app;

	public BuildAppBuilder setName(String name) {
		this.name = name;
		return this;
	}

	public BuildAppBuilder setVersion(String version) {
		this.version = version;
		return this;
	}

	public BuildAppBuilder setCanRelease(boolean canRelease) {
		this.canRelease = canRelease;
		return this;
	}

	public BuildAppBuilder setDeployedDate(String deployedDate) {
		this.deployedDate = deployedDate;
		return this;
	}

	public BuildAppBuilder setErrors(String errors) {
		this.errors = errors;
		return this;
	}

	public BuildAppBuilder setDeployed(boolean deployed) {
		this.deployed = deployed;
		return this;
	}

	public BuildAppBuilder setApp(App app) {
		this.app = app;
		return this;
	}

	public BuildApp build() {
		BuildApp buildApp = new BuildApp();
		buildApp.setUpdatedDate(DateUtils.newDate());
		if (app != null) {
			buildApp.setVersion(app.getVersion());
			buildApp.setName(app.getName());
			buildApp.setErrors(errors);
			buildApp.setDeployed(app.isDeployed());
			buildApp.setDeployedDate(app.getDeployedDate());
		} else {
			buildApp.setVersion(version);
			buildApp.setCanRelease(canRelease);
			buildApp.setDeployed(deployed);
			buildApp.setDeployedDate(deployedDate);
			buildApp.setErrors(errors);
			buildApp.setName(name);
		}
		return buildApp;
	}

}

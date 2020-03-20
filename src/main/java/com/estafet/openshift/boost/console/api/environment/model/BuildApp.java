package com.estafet.openshift.boost.console.api.environment.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ForeignKey;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import com.estafet.openshift.boost.commons.lib.date.DateUtils;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "BUILD_APP")
public class BuildApp {

	@Id
	@Column(name = "BUILD_APP_ID", nullable = false)
	private String name;

	@Column(name = "VERSION", nullable = false)
	private String version;

	@Column(name = "CAN_RELEASE", nullable = false)
	private boolean canRelease;

	@Column(name = "DEPLOYED_DATE", nullable = true)
	private String deployedDate;

	@Column(name = "ERRORS", nullable = true)
	private String errors;

	@Column(name = "DEPLOYED", nullable = false)
	private boolean deployed;

	@Column(name = "UPDATED_DATE", nullable = false)
	private String updatedDate;

	@JsonIgnore
	@ManyToOne
	@JoinColumn(name = "BUILD_ENV_ID", nullable = false, referencedColumnName = "BUILD_ENV_ID", foreignKey = @ForeignKey(name = "BUILD_APP_TO_BUILD_ENV_FK"))
	private BuildEnv appBuildEnv;

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public boolean isCanRelease() {
		return canRelease;
	}

	public void setCanRelease(boolean canRelease) {
		this.canRelease = canRelease;
	}

	public String getErrors() {
		return errors;
	}

	public void setErrors(String errors) {
		this.errors = errors;
	}

	public boolean isDeployed() {
		return deployed;
	}

	public void setDeployed(boolean deployed) {
		this.deployed = deployed;
	}

	public String getDeployedDate() {
		return deployedDate;
	}

	public void setDeployedDate(String deployedDate) {
		this.deployedDate = deployedDate;
	}

	public String getUpdatedDate() {
		return updatedDate;
	}

	public void setUpdatedDate(String updatedDate) {
		this.updatedDate = updatedDate;
	}

	public BuildEnv getAppBuildEnv() {
		return appBuildEnv;
	}

	public void setAppBuildEnv(BuildEnv appBuildEnv) {
		this.appBuildEnv = appBuildEnv;
	}

	public boolean isEqualTo(BuildApp buildApp) {
		if (canRelease != buildApp.canRelease)
			return false;
		if (deployed != buildApp.deployed)
			return false;
		if (deployedDate == null) {
			if (buildApp.deployedDate != null)
				return false;
		} else if (!deployedDate.equals(buildApp.deployedDate))
			return false;
		if (errors == null) {
			if (buildApp.errors != null)
				return false;
		} else if (!errors.equals(buildApp.errors))
			return false;
		if (name == null) {
			if (buildApp.name != null)
				return false;
		} else if (!name.equals(buildApp.name))
			return false;
		if (version == null) {
			if (buildApp.version != null)
				return false;
		} else if (!version.equals(buildApp.version))
			return false;
		return true;
	}

	public void update(BuildApp recentApp) {
		this.version = recentApp.version;
		this.name = recentApp.name;
		this.canRelease = recentApp.canRelease;
		this.deployedDate = recentApp.deployedDate;
		this.errors = recentApp.errors;
		this.deployed = recentApp.deployed;
		this.updatedDate = DateUtils.newDate();		
	}

}

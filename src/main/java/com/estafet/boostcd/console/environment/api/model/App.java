package com.estafet.boostcd.console.environment.api.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ForeignKey;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import com.estafet.openshift.boost.commons.lib.date.DateUtils;
import com.estafet.openshift.boost.messages.environments.EnvironmentApp;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "APP", uniqueConstraints = {
		@UniqueConstraint(columnNames = { "ENV_ID", "APP_ID" }, name = "ENV_APP_KEY") })
public class App {

	@Id
	@SequenceGenerator(name = "APP_ID_SEQ", sequenceName = "APP_ID_SEQ", allocationSize = 1)
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "APP_ID_SEQ")
	@Column(name = "APP_ID")
	private Long id;
	
	@Column(name = "APP_NAME", nullable = false)
	private String name;

	@Column(name = "VERSION", nullable = false)
	private String version;

	@Column(name = "DEPLOYED_DATE", nullable = true)
	private String deployedDate;

	@Column(name = "DEPLOYED", nullable = false)
	private boolean deployed;

	@Column(name = "UPDATED_DATE", nullable = false)
	private String updatedDate;

	@JsonIgnore
	@ManyToOne
	@JoinColumn(name = "ENV_ID", nullable = false, referencedColumnName = "ENV_ID", foreignKey = @ForeignKey(name = "APP_TO_ENV_FK"))
	private Env env;

	public Env getEnv() {
		return env;
	}

	public void setEnv(Env env) {
		this.env = env;
	}

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

	public String getUpdatedDate() {
		return updatedDate;
	}

	public void setUpdatedDate(String updatedDate) {
		this.updatedDate = updatedDate;
	}

	public String getDeployedDate() {
		return deployedDate;
	}

	public void setDeployedDate(String deployedDate) {
		this.deployedDate = deployedDate;
	}

	public boolean isDeployed() {
		return deployed;
	}

	public void setDeployed(boolean deployed) {
		this.deployed = deployed;
	}

	public boolean isEqualTo(App other) {
		if (deployed != other.deployed)
			return false;
		if (deployedDate == null) {
			if (other.deployedDate != null)
				return false;
		} else if (!deployedDate.equals(other.deployedDate))
			return false;
		if (version == null) {
			if (other.version != null)
				return false;
		} else if (!version.equals(other.version))
			return false;
		return true;
	}

	public void merge(App recentApp) {
		this.version = recentApp.version;
		this.name = recentApp.name;
		this.deployedDate = recentApp.deployedDate;
		this.deployed = recentApp.deployed;
		this.updatedDate = DateUtils.newDate();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		App other = (App) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "App [name=" + name + ", version=" + version + ", deployedDate=" + deployedDate + ", deployed="
				+ deployed + ", updatedDate=" + updatedDate + "]";
	}

	
	public EnvironmentApp getEnvironmentApp() {
		EnvironmentApp environmentApp = EnvironmentApp.builder()
				.setDeployed(isDeployed())
				.setDeployedDate(deployedDate)
				.setName(name)
				.setVersion(version)
				.build();
		return environmentApp;
	}
	
	public static AppBuilder builder() {
		return new AppBuilder();
	}
	
	public static class AppBuilder {
		
		private String name;
		private String version;
		private String deployedDate;
		private boolean deployed;
		
		public AppBuilder setName(String name) {
			this.name = name;
			return this;
		}
		
		public AppBuilder setVersion(String version) {
			this.version = version;
			return this;
		}
		
		public AppBuilder setDeployedDate(String deployedDate) {
			this.deployedDate = deployedDate;
			return this;
		}
		
		public AppBuilder setDeployed(boolean deployed) {
			this.deployed = deployed;
			return this;
		}
		
		public App build() {
			App app = new App();
			app.setDeployed(deployed);
			app.setDeployedDate(deployedDate);
			app.setName(name);
			app.setUpdatedDate(DateUtils.newDate());
			app.setVersion(version);
			return app;
		}
		
	}

}

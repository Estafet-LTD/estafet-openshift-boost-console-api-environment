package com.estafet.boostcd.environment.api.model;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ForeignKey;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.Table;

import com.estafet.boostcd.commons.date.DateUtils;
import com.estafet.openshift.boost.messages.environments.Environment;
import com.estafet.openshift.boost.messages.environments.EnvironmentApp;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "Env")
public class Env {

	@Id
	@Column(name = "ENV_ID", nullable = false)
	private String name;
	
	@Column(name = "DISPLAY_NAME", nullable = false)
	private String displayName;

	@Column(name = "UPDATED_DATE", nullable = false)
	private String updatedDate;
	
	@Column(name = "LIVE", nullable = true)
	private Boolean live;
	
	@Column(name = "TESTED", nullable = true)
	private Boolean tested;

	@Column(name = "NEXT_ENV_ID", nullable = true)
	private String next;

	@OneToMany(mappedBy = "env", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
	@OrderBy("name ASC")
	private List<App> apps = new ArrayList<App>();

	@JsonIgnore
	@ManyToOne
	@JoinColumn(name = "PRODUCT_ID", nullable = false, referencedColumnName = "PRODUCT_ID", foreignKey = @ForeignKey(name = "ENV_TO_PRODUCT_FK"))
	private Product product;
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDisplayName() {
		return displayName;
	}

	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	public String getUpdatedDate() {
		return updatedDate;
	}

	public void setUpdatedDate(String updatedDate) {
		this.updatedDate = updatedDate;
	}

	public Boolean getTested() {
		return tested;
	}

	public void setTested(Boolean tested) {
		this.tested = tested;
	}

	public Boolean getLive() {
		return live;
	}

	public void setLive(Boolean live) {
		this.live = live;
	}

	public String getNext() {
		return next;
	}

	public void setNext(String next) {
		this.next = next;
	}

	public List<App> getApps() {
		return apps;
	}

	public void setApps(List<App> apps) {
		this.apps = apps;
	}

	public void setProduct(Product product) {
		this.product = product;
	}

	public Env merge(Env other) {
		displayName = other.displayName;
		live = other.live;
		tested = other.tested;
		next = other.next;
		updatedDate = DateUtils.newDate();
		for (App recentApp : other.getApps()) {
			App app = getApp(recentApp.getName());
			if (app == null) {
				addApp(recentApp);
			} else if (app.getName().equals(recentApp.getName())) {
				app.merge(recentApp);
			}
		}
		return this;
	}

	public Env addApp(App app) {
		app.setEnv(this);
		apps.add(app);
		return this;
	}

	private App getApp(String name) {
		for (App app : apps) {
			if (app.getName().equals(name)) {
				return app;
			}
		}
		return null;
	}

	public boolean changed(Env other) {
		if (!isEqualTo(other)) {
			return true;
		}
		for (App recentApp : other.getApps()) {
			App app = getApp(recentApp.getName());
			if (app == null || !app.isEqualTo(recentApp)) {
				return true;
			}
		}
		return other.getApps().size() != apps.size();	
	}
	
	private boolean isEqualTo(Env other) {
		if (next == null) {
			if (other.next != null)
				return false;
		} else if (!next.equals(other.next))
			return false;
		if (live == null) {
			if (other.live != null)
				return false;
		} else if (!live.equals(other.live))
			return false;
		if (displayName == null) {
			if (other.displayName != null)
				return false;
		} else if (!displayName.equals(other.displayName))
			return false;
		if (tested == null) {
			if (other.tested != null)
				return false;
		} else if (!tested.equals(other.tested))
			return false;
		return true;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
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
		Env other = (Env) obj;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}
	
	public boolean isProd() {
		return name.equals("green") || name.equals("blue");
	}
	
	@Override
	public String toString() {
		return "Env [name=" + name + ", displayName=" + displayName + ", updatedDate=" + updatedDate + ", live=" + live
				+ ", tested=" + tested + ", next=" + next + "]";
	}

	public Environment getEnvironment() {
		Environment environment = Environment.builder()
				.setUpdatedDate(updatedDate)
				.setLive(live)
				.setName(name)
				.setDisplayName(displayName)
				.setTested(tested)
				.setNext(next)
				.build();
		for (App app : apps) {
			EnvironmentApp envApp = EnvironmentApp.builder()
					.setDeployedDate(app.getDeployedDate())
					.setName(app.getName())
					.setVersion(app.getVersion())
					.setDeployed(app.isDeployed())
					.build();
			environment.addApp(envApp);
		}
		return environment;
	}
	
	public static EnvBuilder builder() {
		return new EnvBuilder();
	}
	
	public static class EnvBuilder {
		
		private String name;
		private String displayName;
		private Boolean live;
		private Boolean tested;
		private String next;
				
		public EnvBuilder setDisplayName(String displayName) {
			this.displayName = displayName;
			return this;
		}

		public EnvBuilder setTested(Boolean tested) {
			this.tested = tested;
			return this;
		}

		public EnvBuilder setName(String name) {
			this.name = name;
			return this;
		}
		
		public EnvBuilder setLive(Boolean live) {
			this.live = live;
			return this;
		}
		
		public EnvBuilder setNext(String next) {
			this.next = next;
			return this;
		}
		
		public Env build() {
			Env env = new Env();
			env.setName(name);
			env.setDisplayName(displayName);
			env.setLive(live);
			env.setNext(next);
			env.setTested(tested);
			env.setUpdatedDate(DateUtils.newDate());
			return env;
		}
		
	}

}

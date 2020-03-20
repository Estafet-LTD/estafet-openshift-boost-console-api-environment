package com.estafet.openshift.boost.console.api.environment.model;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.Table;

import com.fasterxml.jackson.databind.ObjectMapper;

@Entity
@Table(name = "BUILD_ENV")
public class BuildEnv {

	@Id
	@Column(name = "BUILD_ENV_ID", nullable = false)
	private String name;

	@Column(name = "UPDATED_DATE", nullable = false)
	private String updatedDate;

	@OneToMany(mappedBy = "appBuildEnv", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
	@OrderBy("name ASC")
	private List<BuildApp> buildApps = new ArrayList<BuildApp>();

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

	public List<BuildApp> getBuildApps() {
		return buildApps;
	}

	public void setBuildApps(List<BuildApp> buildApps) {
		this.buildApps = buildApps;
	}

	public boolean update(BuildEnv buildEnv) {
		if (changed(buildEnv)) {
			for (BuildApp recentApp : buildEnv.getBuildApps()) {
				BuildApp app = getBuildApp(recentApp.getName());
				if (app == null) {
					addApp(recentApp);
				} else if (app.getName().equals(recentApp.getName())) {
					app.update(recentApp);
				}
			}
			return true;	
		}
		return false;
	}

	public BuildEnv addApp(BuildApp app) {
		app.setAppBuildEnv(this);
		buildApps.add(app);
		return this;
	}
	
	private BuildApp getBuildApp(String name) {
		for (BuildApp app : buildApps) {
			if (app.getName().equals(name)) {
				return app;
			}
		}
		return null;
	}

	private boolean changed(BuildEnv buildEnv) {
		for (BuildApp recentApp : buildEnv.getBuildApps()) {
			BuildApp app = getBuildApp(recentApp.getName());
			if (app == null || !app.isEqualTo(recentApp)) {
				return true;
			}
		}
		return false;
	}
	
    public static BuildEnv fromJSON(String message) {
        try {
            return new ObjectMapper().readValue(message, BuildEnv.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public String toJSON() {
        try {
            return new ObjectMapper().writeValueAsString(this);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}

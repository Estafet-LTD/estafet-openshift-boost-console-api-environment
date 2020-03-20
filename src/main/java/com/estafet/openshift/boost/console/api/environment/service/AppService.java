package com.estafet.openshift.boost.console.api.environment.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.estafet.openshift.boost.console.api.environment.model.App;
import com.estafet.openshift.boost.console.api.environment.model.AppFactory;
import com.estafet.openshift.boost.console.api.environment.openshift.OpenShiftClient;
import com.openshift.restclient.model.IService;

@Service
public class AppService {

	@Autowired
	OpenShiftClient client;
	
	@Autowired
	AppFactory appFactory;

	public List<App> getApps(String namespace) {
		List<App> apps = new ArrayList<App>();
		for (IService service : client.getServices(namespace)) {
			apps.add(appFactory.create(namespace, service));
		}
		return apps;
	}

}

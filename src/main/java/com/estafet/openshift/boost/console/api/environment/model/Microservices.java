package com.estafet.openshift.boost.console.api.environment.model;

import java.util.List;

public class Microservices {

	private List<Microservice> microservices;

	public List<Microservice> getMicroservices() {
		return microservices;
	}

	public void setMicroservices(List<Microservice> microservices) {
		this.microservices = microservices;
	}

}

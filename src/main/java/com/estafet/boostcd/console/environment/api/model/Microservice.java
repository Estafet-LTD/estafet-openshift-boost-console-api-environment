package com.estafet.boostcd.console.environment.api.model;

public class Microservice {

	private String name;
	private String repo;
	private String test;
	private boolean expose;
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getRepo() {
		return repo;
	}
	public void setRepo(String repo) {
		this.repo = repo;
	}
	public String getTest() {
		return test;
	}
	public void setTest(String test) {
		this.test = test;
	}
	public boolean isExpose() {
		return expose;
	}
	public void setExpose(boolean expose) {
		this.expose = expose;
	}
	
}

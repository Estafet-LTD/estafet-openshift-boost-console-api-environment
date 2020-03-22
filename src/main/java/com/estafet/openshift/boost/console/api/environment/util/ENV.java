package com.estafet.openshift.boost.console.api.environment.util;

public final class ENV {

	private ENV( ) {}
	
	public static final String PRODUCT = System.getenv("PRODUCT");	
	public static final String OPENSHIFT_HOST_PORT = System.getenv("OPENSHIFT_HOST_PORT");
	public static final String OPENSHIFT_USER = System.getenv("OPENSHIFT_USER");	
	public static final String OPENSHIFT_PASSWORD = System.getenv("OPENSHIFT_PASSWORD");

}

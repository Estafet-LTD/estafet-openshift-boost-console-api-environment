package com.estafet.openshift.boost.console.api.environment.openshift;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.openshift.restclient.ClientBuilder;
import com.openshift.restclient.IClient;
import com.openshift.restclient.ResourceKind;
import com.openshift.restclient.model.IDeploymentConfig;
import com.openshift.restclient.model.IImageStream;
import com.openshift.restclient.model.IResource;
import com.openshift.restclient.model.IService;

import io.opentracing.Span;
import io.opentracing.Tracer;
import io.opentracing.tag.Tags;

@Component
public class OpenShiftClient {

	@Autowired
	private Tracer tracer;

	private IClient getClient() {
		return new ClientBuilder("https://" + System.getenv("OPENSHIFT_HOST_PORT"))
				.withUserName(System.getenv("OPENSHIFT_USER"))
				.withPassword(System.getenv("OPENSHIFT_PASSWORD"))
				.build();
	}

	@SuppressWarnings("deprecation")
	public List<IDeploymentConfig> getDeploymentConfigs(String namespace, String product) {
		Span span = tracer.buildSpan("OpenShiftClient.getDeploymentConfigs").start();
		try {
			span.setBaggageItem("namespace", namespace);
			return getClient().list(ResourceKind.DEPLOYMENT_CONFIG, namespace, "product=" + product);
		} catch (RuntimeException e) {
			throw handleException(span, e);
		} finally {
			span.finish();
		}
	}

	@SuppressWarnings("deprecation")
	public IDeploymentConfig getDeploymentConfig(String namespace, String app) {
		Span span = tracer.buildSpan("OpenShiftClient.getDeploymentConfig").start();
		try {
			span.setBaggageItem("namespace", namespace);
			span.setBaggageItem("app", app);
			return (IDeploymentConfig) getClient().list(ResourceKind.DEPLOYMENT_CONFIG, namespace, "app=" + app).get(0);
		} catch (RuntimeException e) {
			throw handleException(span, e);
		} finally {
			span.finish();
		}
	}

	@SuppressWarnings("deprecation")
	public List<IService> getServices(String namespace) {
		Span span = tracer.buildSpan("OpenShiftClient.getServices").start();
		try {
			span.setBaggageItem("namespace", namespace);
			return getClient().list(ResourceKind.SERVICE, namespace, "product=" + System.getenv("PRODUCT"));
		} catch (RuntimeException e) {
			throw handleException(span, e);
		} finally {
			span.finish();
		}
	}

	@SuppressWarnings("deprecation")
	public IImageStream getImageStream(String namespace, String app) {
		Span span = tracer.buildSpan("OpenShiftClient.getImageStream").start();
		try {
			span.setBaggageItem("namespace", namespace);
			span.setBaggageItem("app", app);
			 for (IResource resource : getClient().list(ResourceKind.IMAGE_STREAM, namespace)) {
				 IImageStream is = (IImageStream)resource;
				 if (is.getName().equals(app)) {
					 return is;
				 }
			 }
			 return null;
		} catch (RuntimeException e) {
			throw handleException(span, e);
		} finally {
			span.finish();
		}
	}

	@SuppressWarnings("deprecation")
	public IService getService(String namespace, String app) {
		Span span = tracer.buildSpan("OpenShiftClient.getService").start();
		try {
			span.setBaggageItem("namespace", namespace);
			span.setBaggageItem("app", app);
			return (IService) getClient().list(ResourceKind.SERVICE, namespace, "app=" + app).get(0);
		} catch (RuntimeException e) {
			throw handleException(span, e);
		} finally {
			span.finish();
		}
	}

	private RuntimeException handleException(Span span, RuntimeException e) {
		Tags.ERROR.set(span, true);
		Map<String, Object> logs = new HashMap<String, Object>();
		logs.put("event", "error");
		logs.put("error.object", e);
		logs.put("message", e.getMessage());
		StringWriter sw = new StringWriter();
		e.printStackTrace(new PrintWriter(sw));
		logs.put("stack", sw.toString());
		span.log(logs);
		return e;
	}

}

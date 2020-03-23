package com.estafet.openshift.boost.console.api.environment.openshift;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import com.estafet.openshift.boost.commons.lib.env.ENV;
import com.openshift.restclient.ClientBuilder;
import com.openshift.restclient.IClient;
import com.openshift.restclient.ResourceKind;
import com.openshift.restclient.capability.CapabilityVisitor;
import com.openshift.restclient.capability.resources.IBuildTriggerable;
import com.openshift.restclient.model.IBuild;
import com.openshift.restclient.model.IBuildConfig;
import com.openshift.restclient.model.IDeploymentConfig;
import com.openshift.restclient.model.IImageStream;
import com.openshift.restclient.model.IProject;
import com.openshift.restclient.model.IService;
import com.openshift.restclient.model.route.IRoute;

import io.opentracing.Span;
import io.opentracing.Tracer;
import io.opentracing.tag.Tags;

@Component
public class OpenShiftClient {

	private static final Logger log = LoggerFactory.getLogger(OpenShiftClient.class);
	
	@Autowired
	private Tracer tracer;

	@Cacheable(cacheNames = { "token" })
	private IClient getClient() {
		return new ClientBuilder("https://" + ENV.OPENSHIFT_HOST_PORT)
				.withUserName(ENV.OPENSHIFT_USER)
				.withPassword(ENV.OPENSHIFT_PASSWORD)
				.build();
	}
	
	@SuppressWarnings("deprecation")
	public Map<String, IProject> getProjects() {
		Span span = tracer.buildSpan("OpenShiftClient.getProjects").start();
		try {
			Map<String, String> labels = new HashMap<String, String>();
			labels.put("product", ENV.PRODUCT);
			labels.put("stage", "true");
			List<IProject> projects = getClient().list(ResourceKind.PROJECT, labels);
			Map<String, IProject> result = new HashMap<String, IProject>();
			for (IProject project : projects) {
				log.info("project - " + project.getName());
				result.put(project.getName(), project);
			}
			return result;
		} catch (RuntimeException e) {
			throw handleException(span, e);
		} finally {
			span.finish();
		}
	}

	@SuppressWarnings("deprecation")
	public boolean isEnvironmentTestPassed(String namespace) {
		Span span = tracer.buildSpan("OpenShiftClient.isEnvironmentTestPassed").start();
		try {			
			span.setBaggageItem("namespace", namespace);
			return Boolean.parseBoolean(getClient().get(ResourceKind.PROJECT, namespace).getLabels().get("test-passed"));
		} catch (RuntimeException e) {
			throw handleException(span, e);
		} finally {
			span.finish();
		}
	}
	
	@SuppressWarnings("deprecation")
	public Map<String, IDeploymentConfig> getDeploymentConfigs(String namespace) {
		Span span = tracer.buildSpan("OpenShiftClient.getDeploymentConfigs").start();
		try {
			Map<String, String> labels = new HashMap<String, String>();
			labels.put("product", ENV.PRODUCT);
			span.setBaggageItem("namespace", namespace);
			List<IDeploymentConfig> dcs = getClient().list(ResourceKind.DEPLOYMENT_CONFIG, namespace, labels);
			Map<String, IDeploymentConfig> result = new HashMap<String, IDeploymentConfig>();
			for (IDeploymentConfig dc : dcs) {
				result.put(dc.getName(), dc);
			}
			return result;
		} catch (RuntimeException e) {
			throw handleException(span, e);
		} finally {
			span.finish();
		}
	}

	@SuppressWarnings("deprecation")
	public Map<String, IService> getServices(String namespace) {
		Span span = tracer.buildSpan("OpenShiftClient.getServices").start();
		try {
			Map<String, String> labels = new HashMap<String, String>();
			labels.put("product", ENV.PRODUCT);
			span.setBaggageItem("namespace", namespace);
			List<IService> services = getClient().list(ResourceKind.SERVICE, namespace, labels);
			Map<String, IService> result = new HashMap<String, IService>();
			for (IService service : services) {
				result.put(service.getName(), service);
			}
			return result;
		} catch (RuntimeException e) {
			throw handleException(span, e);
		} finally {
			span.finish();
		}
	}

	@SuppressWarnings("deprecation")
	public List<IBuild> getBuilds() {
		Span span = tracer.buildSpan("OpenShiftClient.getBuilds").start();
		try {
			Map<String, String> labels = new HashMap<String, String>();
			labels.put("product", ENV.PRODUCT);
			labels.put("type", "build");
			return getClient().list(ResourceKind.BUILD, ENV.PRODUCT + "-cicd", labels);
		} catch (RuntimeException e) {
			throw handleException(span, e);
		} finally {
			span.finish();
		}
	}
	
	@SuppressWarnings("deprecation")
	public Map<String, IImageStream> getImageStreams(String namespace) {
		Span span = tracer.buildSpan("OpenShiftClient.getImageStreams").start();
		try {
			span.setBaggageItem("namespace", namespace);
			List<IImageStream> images = getClient().list(ResourceKind.IMAGE_STREAM, namespace);
			Map<String, IImageStream> result = new HashMap<String, IImageStream>();
			for (IImageStream image : images) {
				result.put(image.getName(), image);
			}
			return result;
		} catch (RuntimeException e) {
			throw handleException(span, e);
		} finally {
			span.finish();
		}
	}
	
	@SuppressWarnings("deprecation")
	public IRoute getRoute() {
		Span span = tracer.buildSpan("OpenShiftClient.getRoute").start();
		try {
			Map<String, String> labels = new HashMap<String, String>();
			labels.put("product", ENV.PRODUCT);
			return (IRoute) getClient().list(ResourceKind.ROUTE, ENV.PRODUCT + "-prod", labels).get(0);
		} catch (RuntimeException e) {
			throw handleException(span, e);
		} finally {
			span.finish();
		}
	}

	@SuppressWarnings("deprecation")
	public void executeBuildPipeline(String app) {
		Span span = tracer.buildSpan("OpenShiftClient.getBuildPipeline").start();
		try {
			span.setBaggageItem("app",app);
			executePipeline((IBuildConfig) getClient().get(ResourceKind.BUILD_CONFIG, "build-" + app, ENV.PRODUCT + "-cicd"));
		} catch (RuntimeException e) {
			throw handleException(span, e);
		} finally {
			span.finish();
		}
	}
	
	@SuppressWarnings("deprecation")
	public void executeBuildAllPipeline() {
		Span span = tracer.buildSpan("OpenShiftClient.getBuildAllPipeline").start();
		try {
			executePipeline((IBuildConfig) getClient().get(ResourceKind.BUILD_CONFIG, "build-all", ENV.PRODUCT + "-cicd"));
		} catch (RuntimeException e) {
			throw handleException(span, e);
		} finally {
			span.finish();
		}
	}
	
	@SuppressWarnings("deprecation")
	public void executeReleasePipeline(String app) {
		Span span = tracer.buildSpan("OpenShiftClient.getReleasePipeline").start();
		try {
			span.setBaggageItem("app",app);
			executePipeline((IBuildConfig) getClient().get(ResourceKind.BUILD_CONFIG, "release-" + app, ENV.PRODUCT + "-cicd"));
		} catch (RuntimeException e) {
			throw handleException(span, e);
		} finally {
			span.finish();
		}
	}
	
	@SuppressWarnings("deprecation")
	public void executeReleaseAllPipeline() {
		Span span = tracer.buildSpan("OpenShiftClient.getReleasePipeline").start();
		try {
			executePipeline((IBuildConfig) getClient().get(ResourceKind.BUILD_CONFIG, "release-all", ENV.PRODUCT + "-cicd"));
		} catch (RuntimeException e) {
			throw handleException(span, e);
		} finally {
			span.finish();
		}
	}
	
	@SuppressWarnings("deprecation")
	public void executePromoteToProdPipeline(String app) {
		Span span = tracer.buildSpan("OpenShiftClient.getPromoteToProdPipeline").start();
		try {
			span.setBaggageItem("app", app);
			executePipeline((IBuildConfig) getClient().get(ResourceKind.BUILD_CONFIG, "promote-to-prod-" + app,
					ENV.PRODUCT + "-cicd"));
		} catch (RuntimeException e) {
			throw handleException(span, e);
		} finally {
			span.finish();
		}
	}

	@SuppressWarnings("deprecation")
	public void executePromoteAllToProdPipeline() {
		Span span = tracer.buildSpan("OpenShiftClient.getPromoteAllToProdPipeline").start();
		try {
			executePipeline((IBuildConfig) getClient().get(ResourceKind.BUILD_CONFIG, "promote-all-to-prod", ENV.PRODUCT + "-cicd"));
		} catch (RuntimeException e) {
			throw handleException(span, e);
		} finally {
			span.finish();
		}
	}
	
	@SuppressWarnings("deprecation")
	public void executePromotePipeline(String env, String app) {
		Span span = tracer.buildSpan("OpenShiftClient.getPromotePipeline").start();
		try {
			span.setBaggageItem("app", app);
			executePipeline(getClient().get(ResourceKind.BUILD_CONFIG, "promote-" + env + "-" + app,
					ENV.PRODUCT + "-cicd"));
		} catch (RuntimeException e) {
			throw handleException(span, e);
		} finally {
			span.finish();
		}
	}

	@SuppressWarnings("deprecation")
	public void executePromoteAllPipeline(String env) {
		Span span = tracer.buildSpan("OpenShiftClient.getPromoteAllPipeline").start();
		try {
			executePipeline(getClient().get(ResourceKind.BUILD_CONFIG, "promote-all-" + env, ENV.PRODUCT + "-cicd"));
		} catch (RuntimeException e) {
			throw handleException(span, e);
		} finally {
			span.finish();
		}
	}

	@SuppressWarnings("deprecation")
	public void executeTestPipeline(String env) {
		Span span = tracer.buildSpan("OpenShiftClient.getTestPipeline").start();
		try {
			executePipeline((IBuildConfig) getClient().get(ResourceKind.BUILD_CONFIG, "qa-" + env, ENV.PRODUCT + "-cicd"));
		} catch (RuntimeException e) {
			throw handleException(span, e);
		} finally {
			span.finish();
		}
	}
	
	@SuppressWarnings("deprecation")
	public void executePromoteToLivePipeline() {
		Span span = tracer.buildSpan("OpenShiftClient.getPromoteToLivePipeline").start();
		try {
			executePipeline((IBuildConfig) getClient().get(ResourceKind.BUILD_CONFIG, "promote-to-live", ENV.PRODUCT + "-cicd"));
		} catch (RuntimeException e) {
			throw handleException(span, e);
		} finally {
			span.finish();
		}
	}
	
	private void executePipeline(IBuildConfig pipeline) {
		pipeline.accept(new CapabilityVisitor<IBuildTriggerable, IBuild>() {
            @Override
            public IBuild visit(IBuildTriggerable capability) {
                return capability.trigger();
            }
        }, null);
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

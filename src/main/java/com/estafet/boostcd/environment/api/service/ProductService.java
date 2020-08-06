package com.estafet.boostcd.environment.api.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.estafet.boostcd.commons.env.ENV;
import com.estafet.boostcd.environment.api.dao.ProductDAO;
import com.estafet.boostcd.environment.api.model.App;
import com.estafet.boostcd.environment.api.model.AppFactory;
import com.estafet.boostcd.environment.api.model.Env;
import com.estafet.boostcd.environment.api.model.Product;
import com.estafet.boostcd.openshift.OpenShiftClient;
import com.openshift.restclient.model.IDeploymentConfig;
import com.openshift.restclient.model.IImageStream;
import com.openshift.restclient.model.IProject;
import com.openshift.restclient.model.IService;

@Component
public class ProductService {

	private static final Logger log = LoggerFactory.getLogger(ProductService.class);

	@Autowired
	private OpenShiftClient client;

	@Autowired
	private AppFactory appFactory;

	@Autowired
	private ProductDAO productDAO;

	@Transactional(readOnly = true)
	public List<Product> getProducts() {
		return productDAO.getProducts();
	}
	
	@Transactional
	public List<Product> update() {
		List<Product> products = new ArrayList<Product>();
		for (Product product : productDAO.getProducts()) {
			products.add(product.addEnvs(getEnvs(product)));
		}
		return products;
	}

	private List<Env> getEnvs(Product product) {
		return getEnvs(product, ENV.build(product.getProductId()), client.getProjects(product.getProductId()),
				new ArrayList<Env>());
	}

	private List<Env> getEnvs(Product product, String namespace, Map<String, IProject> projects, List<Env> envs) {
		IProject project = projects.get(namespace);
		String next = project.getLabels().get("next");
		if (!next.equals(product.getProductId() + "-end")) {
			envs.add(createEnv(product, project, next));
			return getEnvs(product, next, projects, envs);
		} else if (namespace.equals(ENV.prod(product.getProductId()))) {
			envs.add(createProdEnv(product, "green", project));
			envs.add(createProdEnv(product, "blue", project));
			return envs;
		}
		throw new RuntimeException("cannot create Env for namespace - " + namespace);
	}

	private Env createProdEnv(Product product, String name, IProject project) {
		log.debug("createProEnv - " + name);
		Env env = Env.builder()
					.setName(name)
					.setDisplayName(isLive(product, name) ? "Live" : "Staging")
					.setLive(isLive(product, name))
					.setTested(prodTestedStatus(product, name, project)).build();
		return addApps(product, env, ENV.prod(product.getProductId()));
	}

	public Boolean prodTestedStatus(Product product, String name, IProject project) {
		if (isLive(product, name)) {
			return null;
		} else {
			return client.isEnvironmentTestPassed(project);
		}
	}

	private Env createEnv(Product product, IProject project, String next) {
		log.debug("createEnv - " + project.getName());
		Env env = Env.builder().setName(envName(product, project.getName()))
				.setDisplayName(project.getLabels().get("display"))
				.setNext(envName(product, next))
				.setTested(testedStatus(product, project)).build();
		return addApps(product, env, project.getName());
	}

	private String envName(Product product, String namespace) {
		return namespace.substring(product.getProductId().length() + 1);
	}	
	
	public Boolean testedStatus(Product product, IProject project) {
		if (project.getName().equals(ENV.build(product.getProductId()))) {
			return null;
		}
		return client.isEnvironmentTestPassed(project);
	}

	private Env addApps(Product product, Env env, String namespace) {
		Map<String, IDeploymentConfig> dcs = client.getDeploymentConfigs(product.getProductId(), namespace);
		Map<String, IService> services = client.getServices(product.getProductId(), namespace);
		for (String appName : dcs.keySet()) {
			try {
				App app = null;
				IDeploymentConfig dc = dcs.get(appName);
				IService service = services.get(appName);
				if (env.getName().equals("build")) {
					Map<String, IImageStream> cicdImages = client.getCICDImageStreams(product.getProductId());
					Map<String, IImageStream> images = client.getImageStreams(product.getProductId(), namespace);
					app = appFactory.getBuildApp(dc, service, images.get(appName), cicdImages.get(appName));
				} else {
					app = appFactory.getApp(dc, service);
				}
				if (app != null) {
					if ((env.isProd() && appName.startsWith(env.getName())) || !env.isProd()) {
						env.addApp(app);
					}
				} else {
					log.warn("could not construct app for - " + appName);
				}
			} catch (RuntimeException e) {
				log.warn("There was a problem when constructing app - " + appName, e);
			}
		}
		return env;
	}

	private boolean isLive(Product product, String name) {
		return client.getRoute(product.getProductId()).getServiceName().startsWith(name);
	}

	@Transactional
	public Product update(Product product) {
		Product saved = productDAO.getProduct(product.getProductId());
		if (saved != null) {
			productDAO.update(saved.merge(product));
		} else {
			productDAO.create(product);
		}
		return product;
	}

	@Transactional(readOnly = true)
	public Product getProduct(String product) {
		return productDAO.getProduct(product);
	}

}

package com.estafet.boostcd.environment.api.scheduled;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.estafet.boostcd.environment.api.jms.EnvProducer;
import com.estafet.boostcd.environment.api.model.Env;
import com.estafet.boostcd.environment.api.service.EnvironmentService;

@Component
public class EnvScheduler {

	private static final Logger log = LoggerFactory.getLogger(EnvScheduler.class);

	@Autowired
	private EnvironmentService environmentService;

	@Autowired
	private EnvProducer envProducer;

	@Scheduled(fixedRate = 30000)
	public void execute() {
		log.info("refreshing environment data");
		for (Env env : environmentService.updateEnvs()) {
			envProducer.sendMessage(env.getEnvironment());
		}
		log.info("environment data refreshed");
	}

}
package com.estafet.boostcd.environment.api.jms;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Component;

import com.estafet.openshift.boost.messages.environments.Environment;

@Component
public class EnvProducer {

	private static final Logger log = LoggerFactory.getLogger(EnvProducer.class);
	
    @Autowired
    private JmsTemplate jmsTemplate;

    public void sendMessage(Environment env) {
    	log.debug(env.toJSON());
        jmsTemplate.setPubSubDomain(true);
        jmsTemplate.convertAndSend("env.topic", env.toJSON());
    }

}

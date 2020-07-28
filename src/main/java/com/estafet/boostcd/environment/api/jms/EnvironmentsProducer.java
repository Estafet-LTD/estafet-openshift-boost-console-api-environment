package com.estafet.boostcd.environment.api.jms;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Component;

import com.estafet.openshift.boost.messages.environments.Environments;

@Component
public class EnvironmentsProducer {

	private static final Logger log = LoggerFactory.getLogger(EnvironmentsProducer.class);
	
    @Autowired
    private JmsTemplate jmsTemplate;

    public void sendMessage(Environments environments) {
    	log.debug(environments.toJSON());
        jmsTemplate.setPubSubDomain(true);
        jmsTemplate.convertAndSend("environments.topic", environments.toJSON());
    }

}

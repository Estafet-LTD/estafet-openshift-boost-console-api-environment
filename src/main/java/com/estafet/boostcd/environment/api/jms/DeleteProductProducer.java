package com.estafet.boostcd.environment.api.jms;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Component;

import com.estafet.openshift.boost.messages.environments.Environments;

@Component
public class DeleteProductProducer {

	private static final Logger log = LoggerFactory.getLogger(DeleteProductProducer.class);
	
    @Autowired
    private JmsTemplate jmsTemplate;

    public void sendMessage(Environments environments) {
    	log.info(environments.toJSON());
        jmsTemplate.setPubSubDomain(true);
        jmsTemplate.convertAndSend("delete.environments.topic", environments.toJSON());
    }

}

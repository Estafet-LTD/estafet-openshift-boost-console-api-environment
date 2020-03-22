package com.estafet.openshift.boost.console.api.environment.jms;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Component;

import com.estafet.openshift.boost.messages.environments.Environment;

@Component
public class EnvProducer {

    @Autowired
    private JmsTemplate jmsTemplate;

    public void sendMessage(Environment env) {
        jmsTemplate.setPubSubDomain(true);
        jmsTemplate.convertAndSend("env.topic", env.toJSON());
    }

}

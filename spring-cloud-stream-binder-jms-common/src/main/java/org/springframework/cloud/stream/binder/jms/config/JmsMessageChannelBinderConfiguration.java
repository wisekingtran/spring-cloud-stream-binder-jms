/*
 * Class: JmsMessageChannelBinderConfiguration
 *
 * Created on Nov 26, 2020
 *
 * (c) Copyright Swiss Post Solutions Ltd, unpublished work
 * All use, disclosure, and/or reproduction of this material is prohibited
 * unless authorized in writing.  All Rights Reserved.
 * Rights in this program belong to:
 * Swiss Post Solution.
 * Floor 4-5-8, ICT Tower, Quang Trung Software City
 */
package org.springframework.cloud.stream.binder.jms.config;

import javax.jms.ConnectionFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.stream.binder.ExtendedConsumerProperties;
import org.springframework.cloud.stream.binder.ExtendedProducerProperties;
import org.springframework.cloud.stream.binder.jms.JMSMessageChannelBinder;
import org.springframework.cloud.stream.provisioning.ProvisioningProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.core.JmsTemplate;

@Configuration
@EnableConfigurationProperties(JmsExtendedBindingProperties.class)
public class JmsMessageChannelBinderConfiguration {

    @Autowired
    private ProvisioningProvider<ExtendedConsumerProperties<JmsConsumerProperties>, ExtendedProducerProperties<JmsProducerProperties>> provisioningProvider;

    @Autowired
    private ConnectionFactory connectionFactory;

    @Autowired
    private JmsExtendedBindingProperties jmsExtendedBindingProperties;

    @Bean
    JMSMessageChannelBinder jmsMessageChannelBinder(JmsTemplate jmsTemplate)
            throws Exception {

        JMSMessageChannelBinder jmsMessageChannelBinder = new JMSMessageChannelBinder(
            provisioningProvider, jmsTemplate, connectionFactory);
        jmsMessageChannelBinder
            .setExtendedBindingProperties(jmsExtendedBindingProperties);

        return jmsMessageChannelBinder;
    }

}
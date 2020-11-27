/*
 *  Copyright 2002-2017 the original author or authors.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *		http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.springframework.cloud.stream.binder.jms;

import javax.jms.ConnectionFactory;

import org.springframework.cloud.stream.binder.AbstractMessageChannelBinder;
import org.springframework.cloud.stream.binder.BinderSpecificPropertiesProvider;
import org.springframework.cloud.stream.binder.ExtendedConsumerProperties;
import org.springframework.cloud.stream.binder.ExtendedProducerProperties;
import org.springframework.cloud.stream.binder.ExtendedPropertiesBinder;
import org.springframework.cloud.stream.binder.jms.config.JmsConsumerProperties;
import org.springframework.cloud.stream.binder.jms.config.JmsExtendedBindingProperties;
import org.springframework.cloud.stream.binder.jms.config.JmsProducerProperties;
import org.springframework.cloud.stream.provisioning.ConsumerDestination;
import org.springframework.cloud.stream.provisioning.ProducerDestination;
import org.springframework.cloud.stream.provisioning.ProvisioningProvider;
import org.springframework.integration.core.MessageProducer;
import org.springframework.integration.jms.JmsSendingMessageHandler;
import org.springframework.integration.jms.dsl.Jms;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.listener.DefaultMessageListenerContainer;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHandler;

public class JMSMessageChannelBinder extends
        AbstractMessageChannelBinder<ExtendedConsumerProperties<JmsConsumerProperties>, ExtendedProducerProperties<JmsProducerProperties>, ProvisioningProvider<ExtendedConsumerProperties<JmsConsumerProperties>, ExtendedProducerProperties<JmsProducerProperties>>>
        implements
        ExtendedPropertiesBinder<MessageChannel, JmsConsumerProperties, JmsProducerProperties> {

    private JmsExtendedBindingProperties extendedBindingProperties = new JmsExtendedBindingProperties();

    private final ConnectionFactory connectionFactory;

    public JMSMessageChannelBinder(
            ProvisioningProvider<ExtendedConsumerProperties<JmsConsumerProperties>, ExtendedProducerProperties<JmsProducerProperties>> provisioningProvider,
            JmsTemplate jmsTemplate, ConnectionFactory connectionFactory) {
        super(null, provisioningProvider);
        this.connectionFactory = connectionFactory;
    }

    public void setExtendedBindingProperties(
        JmsExtendedBindingProperties extendedBindingProperties) {
        this.extendedBindingProperties = extendedBindingProperties;
    }

    @Override
    protected MessageHandler createProducerMessageHandler(
        ProducerDestination producerDestination,
        ExtendedProducerProperties<JmsProducerProperties> producerProperties,
        MessageChannel errorChannel) throws Exception {

        final JmsSendingMessageHandler handler = Jms
            .outboundAdapter(connectionFactory)
            .configureJmsTemplate(s -> s.pubSubDomain(true))
            .destination(producerDestination.getName()).get();
        {
            handler.setBeanFactory(getBeanFactory());
        }

        return handler;
    }

    @Override
    protected MessageProducer createConsumerEndpoint(
        ConsumerDestination consumerDestination,
        String group,
        ExtendedConsumerProperties<JmsConsumerProperties> properties)
            throws Exception {

        DefaultMessageListenerContainer listenerContainer = Jms
            .container(connectionFactory, consumerDestination.getName()).get();

        return Jms.messageDrivenChannelAdapter(listenerContainer).get();
    }

    @Override
    public JmsConsumerProperties getExtendedConsumerProperties(
        String channelName) {
        return extendedBindingProperties
            .getExtendedConsumerProperties(channelName);
    }

    @Override
    public JmsProducerProperties getExtendedProducerProperties(
        String channelName) {

        return extendedBindingProperties
            .getExtendedProducerProperties(channelName);
    }

    @Override
    public String getDefaultsPrefix() {
        return extendedBindingProperties.getDefaultsPrefix();
    }

    @Override
    public Class<? extends BinderSpecificPropertiesProvider> getExtendedPropertiesEntryClass() {
        return extendedBindingProperties.getExtendedPropertiesEntryClass();
    }

}

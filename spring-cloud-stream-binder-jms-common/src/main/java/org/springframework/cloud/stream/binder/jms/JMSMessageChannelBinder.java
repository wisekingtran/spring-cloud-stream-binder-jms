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

import java.util.ArrayList;
import java.util.List;

import javax.jms.ConnectionFactory;
import javax.jms.IllegalStateException;

import org.springframework.cloud.stream.binder.AbstractMessageChannelBinder;
import org.springframework.cloud.stream.binder.BinderSpecificPropertiesProvider;
import org.springframework.cloud.stream.binder.ExtendedBindingProperties;
import org.springframework.cloud.stream.binder.ExtendedConsumerProperties;
import org.springframework.cloud.stream.binder.ExtendedProducerProperties;
import org.springframework.cloud.stream.binder.ExtendedPropertiesBinder;
import org.springframework.cloud.stream.binder.jms.config.JmsConsumerProperties;
import org.springframework.cloud.stream.binder.jms.config.JmsExtendedBindingProperties;
import org.springframework.cloud.stream.binder.jms.config.JmsProducerProperties;
import org.springframework.cloud.stream.binder.jms.provisioning.JmsProducerDestination;
import org.springframework.cloud.stream.provisioning.ConsumerDestination;
import org.springframework.cloud.stream.provisioning.ProducerDestination;
import org.springframework.cloud.stream.provisioning.ProvisioningProvider;
import org.springframework.integration.core.MessageProducer;
import org.springframework.integration.jms.JmsSendingMessageHandler;
import org.springframework.integration.jms.dsl.Jms;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.listener.DefaultMessageListenerContainer;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHandler;
import org.springframework.messaging.MessagingException;
import org.springframework.util.Assert;

public class JMSMessageChannelBinder extends
        AbstractMessageChannelBinder<ExtendedConsumerProperties<JmsConsumerProperties>, ExtendedProducerProperties<JmsProducerProperties>, ProvisioningProvider<ExtendedConsumerProperties<JmsConsumerProperties>, ExtendedProducerProperties<JmsProducerProperties>>>
        implements
        ExtendedPropertiesBinder<MessageChannel, JmsConsumerProperties, JmsProducerProperties> {

    class MessageHandlerChain implements MessageHandler {

        private final List<JmsSendingMessageHandler> handlers;

        public MessageHandlerChain(
                final List<JmsSendingMessageHandler> handlers) {
            this.handlers = handlers;
        }

        @Override
        public void handleMessage(final Message<?> message)
                throws MessagingException {
            if (this.handlers != null) {
                this.handlers.forEach(h -> h.handleMessage(message));
            }
        }

    }

    private final ConnectionFactory connectionFactory;

    private ExtendedBindingProperties<JmsConsumerProperties, JmsProducerProperties> extendedBindingProperties = new JmsExtendedBindingProperties();

    public JMSMessageChannelBinder(
            final ProvisioningProvider<ExtendedConsumerProperties<JmsConsumerProperties>, ExtendedProducerProperties<JmsProducerProperties>> provisioningProvider,
            final JmsTemplate jmsTemplate,
            final ConnectionFactory connectionFactory) {
        super(null, provisioningProvider);
        this.connectionFactory = connectionFactory;
    }

    @Override
    protected MessageProducer createConsumerEndpoint(
        final ConsumerDestination consumerDestination,
        final String group,
        final ExtendedConsumerProperties<JmsConsumerProperties> properties)
            throws Exception {

        final DefaultMessageListenerContainer listenerContainer = Jms
            .container(this.connectionFactory, consumerDestination.getName())
            .get();

        return Jms.messageDrivenChannelAdapter(listenerContainer).get();
    }

    @Override
    protected MessageHandler createProducerMessageHandler(
        final ProducerDestination producerDestination,
        final ExtendedProducerProperties<JmsProducerProperties> producerProperties,
        final MessageChannel errorChannel) throws Exception {

        Assert.isInstanceOf(JmsProducerDestination.class, producerDestination);

        final JmsProducerDestination jmsProducerDestination = (JmsProducerDestination) producerDestination;

        final String topicName = jmsProducerDestination.getName();

        if (topicName != null && !topicName.isEmpty()) {

            // Topic is take the precedence for binding

            final JmsSendingMessageHandler handler = Jms
                .outboundAdapter(this.connectionFactory)
                .configureJmsTemplate(s -> s.pubSubDomain(true))
                .destination(topicName).get();
            {
                handler.setBeanFactory(this.getBeanFactory());
            }
            return handler;
        }

        final String[] queueNames = jmsProducerDestination.getQueueNames();
        if (queueNames == null || queueNames.length == 0) {
            throw new IllegalStateException(
                "Both topic and queue are undefined under producer destination. At least one of them must be available for binding!");
        }
        final List<JmsSendingMessageHandler> handlers = new ArrayList<>();
        final MessageHandlerChain chainHandler = new MessageHandlerChain(
            handlers);

        for (final String queueName : queueNames) {
            final JmsSendingMessageHandler handler = Jms
                .outboundAdapter(this.connectionFactory).destination(queueName)
                .get();
            {
                handler.setBeanFactory(this.getBeanFactory());
            }
            handlers.add(handler);
        }
        return chainHandler;
    }

    @Override
    public String getDefaultsPrefix() {
        return this.extendedBindingProperties.getDefaultsPrefix();
    }

    @Override
    public JmsConsumerProperties getExtendedConsumerProperties(
        final String channelName) {
        return this.extendedBindingProperties
            .getExtendedConsumerProperties(channelName);
    }

    @Override
    public JmsProducerProperties getExtendedProducerProperties(
        final String channelName) {

        return this.extendedBindingProperties
            .getExtendedProducerProperties(channelName);
    }

    @Override
    public Class<? extends BinderSpecificPropertiesProvider> getExtendedPropertiesEntryClass() {
        return this.extendedBindingProperties.getExtendedPropertiesEntryClass();
    }

    public void setExtendedBindingProperties(
        final ExtendedBindingProperties<JmsConsumerProperties, JmsProducerProperties> extendedBindingProperties) {
        this.extendedBindingProperties = extendedBindingProperties;
    }

}

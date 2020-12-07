/*
 *  Copyright 2002-2017 the original author or authors.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.springframework.cloud.stream.binder.jms.activemq;

import java.util.Collection;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.Queue;
import javax.jms.Session;
import javax.jms.Topic;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.stream.binder.ExtendedConsumerProperties;
import org.springframework.cloud.stream.binder.ExtendedProducerProperties;
import org.springframework.cloud.stream.binder.jms.config.JmsCommonProperties;
import org.springframework.cloud.stream.binder.jms.config.JmsConsumerProperties;
import org.springframework.cloud.stream.binder.jms.config.JmsProducerProperties;
import org.springframework.cloud.stream.binder.jms.provisioning.JmsConsumerDestination;
import org.springframework.cloud.stream.binder.jms.provisioning.JmsProducerDestination;
import org.springframework.cloud.stream.binder.jms.utils.Constants;
import org.springframework.cloud.stream.binder.jms.utils.DestinationNameResolver;
import org.springframework.cloud.stream.binder.jms.utils.DestinationNames;
import org.springframework.cloud.stream.provisioning.ConsumerDestination;
import org.springframework.cloud.stream.provisioning.ProducerDestination;
import org.springframework.cloud.stream.provisioning.ProvisioningProvider;
import org.springframework.jms.support.JmsUtils;
import org.springframework.util.Assert;

/**
 * {@link ProvisioningProvider} for ActiveMQ.
 *
 */
public class ActiveMQQueueProvisioner implements
        ProvisioningProvider<ExtendedConsumerProperties<JmsConsumerProperties>, ExtendedProducerProperties<JmsProducerProperties>> {

    private static final Logger LOGGER = LoggerFactory
        .getLogger(ActiveMQQueueProvisioner.class);

    private final ConnectionFactory connectionFactory;

    private final DestinationNameResolver destinationNameResolver;

    public ActiveMQQueueProvisioner(final ConnectionFactory connectionFactory,
            final DestinationNameResolver destinationNameResolver) {

        this.connectionFactory = connectionFactory;
        this.destinationNameResolver = destinationNameResolver;
    }

    private Queue createQueue(
        final String destinationPattern,
        final String topicName,
        final Session session,
        final String consumerGroup) throws JMSException {

        final String queueName = StringUtils
            .countMatches(destinationPattern, Constants.PLACEHOLDER_STRING) == 2
                    ? String
                        .format(destinationPattern, consumerGroup, topicName)
                    : String.format(destinationPattern, topicName);
        final Queue queue = session.createQueue(queueName);
        //TODO: Understand why a producer is required to actually create the queue, it's not mentioned in ActiveMQ docs
        session.createProducer(queue).close();
        return queue;
    }

    @Override
    public ConsumerDestination provisionConsumerDestination(
        final String destinationName,
        final String group,
        final ExtendedConsumerProperties<JmsConsumerProperties> properties) {

        Assert.hasText(
            destinationName,
            "Destination should be at least one non-whitespace character");

        final String groupName = this.destinationNameResolver
            .buildGroupName(group, properties);
        final JmsCommonProperties extension = properties.getExtension();
        final String queuePattern = extension.getQueuePattern();

        if (!extension.isBindQueueOnly()) {
            this.provisionTopic(extension.getTopicPattern(), destinationName);
        }
        final Queue[] queues = this.provisionConsumerForGroups(
            queuePattern,
            destinationName,
            groupName);

        return new JmsConsumerDestination(
            (queues != null) && (queues.length > 0) ? queues[0] : null);
    }

    private Queue[] provisionConsumerForGroups(
        final String consumerDestinationPattern,
        final String topicName,
        final String... consumerGroups) {

        Connection activeMQConnection;
        Session session;
        Queue[] groups = null;
        try {
            activeMQConnection = this.connectionFactory.createConnection();
            session = activeMQConnection
                .createSession(true, Session.CLIENT_ACKNOWLEDGE);
            if (ArrayUtils.isNotEmpty(consumerGroups)) {
                groups = new Queue[consumerGroups.length];
                for (int i = 0; i < consumerGroups.length; i++) {
                    /*
                     * By default, ActiveMQ consumer queues are named 'Consumer.*.VirtualTopic.',
                     * therefore we must remove '.' from the consumer group name if present.
                     * For example, anonymous consumer groups are named 'anonymous.*' by default.
                     */
                    groups[i] = this.createQueue(
                        consumerDestinationPattern,
                        topicName,
                        session,
                        consumerGroups[i].replaceAll("\\.", "_"));
                }
            }

            JmsUtils.commitIfNecessary(session);
            JmsUtils.closeSession(session);
            JmsUtils.closeConnection(activeMQConnection);
        }
        catch (final JMSException e) {
            throw new IllegalStateException(e);
        }

        return groups;
    }

    @Override
    public ProducerDestination provisionProducerDestination(
        final String destinationName,
        final ExtendedProducerProperties<JmsProducerProperties> properties) {

        final JmsProducerProperties extension = properties.getExtension();
        final Collection<DestinationNames> topicAndQueueNames = this.destinationNameResolver
            .resolveDestinationNameForRequiredGroups(
                destinationName,
                properties);

        String[] queueNames = null;
        String topicName = null;
        for (final DestinationNames destinationNames : topicAndQueueNames) {
            try {
                if (!extension.isBindQueueOnly()) {
                    final Topic topic = this.provisionTopic(
                        extension.getTopicPattern(),
                        destinationNames.getDestinationName());
                    topicName = topic.getTopicName();

                }

                final Queue[] queues = this.provisionConsumerForGroups(
                    extension.getQueuePattern(),
                    destinationNames.getDestinationName(),
                    destinationNames.getGroupNames());

                if (queues != null) {
                    queueNames = new String[queues.length];
                    for (int i = 0; i < queues.length; i++) {
                        queueNames[i] = queues[i].getQueueName();
                    }
                }
            }
            catch (final JMSException e) {
                ActiveMQQueueProvisioner.LOGGER.error(
                    "Error occured when provision destination [{}]",
                    destinationNames,
                    e);
            }
        }
        final JmsProducerDestination jmsProducerDestination = new JmsProducerDestination(
            topicName);
        jmsProducerDestination.setQueueNames(queueNames);
        return jmsProducerDestination;
    }

    private Topic provisionTopic(
        final String topicPattern,
        final String topicName) {
        Connection activeMQConnection;
        Session session;
        Topic topic = null;
        try {
            activeMQConnection = this.connectionFactory.createConnection();
            session = activeMQConnection
                .createSession(true, Session.CLIENT_ACKNOWLEDGE);
            topic = session.createTopic(String.format(topicPattern, topicName));

            JmsUtils.commitIfNecessary(session);
            JmsUtils.closeSession(session);
            JmsUtils.closeConnection(activeMQConnection);
        }
        catch (final JMSException e) {
            throw new IllegalStateException(e);
        }
        return topic;
    }
}

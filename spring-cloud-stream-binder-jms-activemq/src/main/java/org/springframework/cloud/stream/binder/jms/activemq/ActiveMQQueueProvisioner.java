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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.Queue;
import javax.jms.Session;
import javax.jms.Topic;

import org.apache.commons.lang.ArrayUtils;
import org.springframework.cloud.stream.binder.ExtendedConsumerProperties;
import org.springframework.cloud.stream.binder.ExtendedProducerProperties;
import org.springframework.cloud.stream.binder.jms.config.JmsConsumerProperties;
import org.springframework.cloud.stream.binder.jms.config.JmsProducerProperties;
import org.springframework.cloud.stream.binder.jms.provisioning.JmsConsumerDestination;
import org.springframework.cloud.stream.binder.jms.provisioning.JmsProducerDestination;
import org.springframework.cloud.stream.binder.jms.utils.DestinationNameResolver;
import org.springframework.cloud.stream.binder.jms.utils.DestinationNames;
import org.springframework.cloud.stream.provisioning.ConsumerDestination;
import org.springframework.cloud.stream.provisioning.ProducerDestination;
import org.springframework.cloud.stream.provisioning.ProvisioningException;
import org.springframework.cloud.stream.provisioning.ProvisioningProvider;
import org.springframework.jms.support.JmsUtils;

/**
 * {@link ProvisioningProvider} for ActiveMQ.
 *
 * @author José Carlos Valero
 * @author Ilayaperumal Gopinathan
 * @since 1.1
 */
public class ActiveMQQueueProvisioner implements
        ProvisioningProvider<ExtendedConsumerProperties<JmsConsumerProperties>, ExtendedProducerProperties<JmsProducerProperties>> {

    private final ConnectionFactory connectionFactory;

    private final DestinationNameResolver destinationNameResolver;

    public ActiveMQQueueProvisioner(ConnectionFactory connectionFactory,
            DestinationNameResolver destinationNameResolver) {

        this.connectionFactory = connectionFactory;
        this.destinationNameResolver = destinationNameResolver;
    }

    @Override
    public ProducerDestination provisionProducerDestination(
        final String name,
        ExtendedProducerProperties<JmsProducerProperties> properties) {

        final JmsProducerProperties extension = properties.getExtension();
        Collection<DestinationNames> topicAndQueueNames = this.destinationNameResolver
            .resolveTopicAndQueueNameForRequiredGroups(name, properties);

        final List<String> queueNames = new ArrayList<>();

        for (DestinationNames destinationNames : topicAndQueueNames) {

            //TODO -- Need to understand why the original code require to create a topic. 
            //        ActiveMQQueueProvisioner should provision queue only
            
            Topic topic = provisionTopic(
                extension.getTopicPattern(),
                destinationNames.getTopicName());
            Queue[] queues = provisionConsumerGroup(
                extension.getQueuePattern(),
                destinationNames.getTopicName(),
                destinationNames.getGroupNames());

            if (queues != null) {
                for (Queue q : queues) {
                    try {
                        queueNames.add(q.getQueueName());
                    }
                    catch (JMSException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
            }
        }
        return new JmsProducerDestination(
            queueNames.toArray(new String[queueNames.size()]));
    }

    @Override
    public ConsumerDestination provisionConsumerDestination(
        String name,
        String group,
        ExtendedConsumerProperties<JmsConsumerProperties> properties) {

        String groupName = this.destinationNameResolver
            .resolveQueueNameForInputGroup(group, properties);
        String topicName = this.destinationNameResolver
            .resolveQueueNameForInputGroup(name, properties);
        final JmsConsumerProperties extension = properties.getExtension();
        String queuePattern = extension.getQueuePattern();

        provisionTopic(extension.getTopicPattern(), topicName);
        final Queue[] queues = provisionConsumerGroup(
            queuePattern,
            topicName,
            groupName);

        //DLQ_NAME
        Session session;
        Connection connection;
        try {
            connection = connectionFactory.createConnection();
            session = connection.createSession(true, 1);
        }
        catch (JMSException e) {
            throw new ProvisioningException("Provisioning failed",
                JmsUtils.convertJmsAccessException(e));
        }
        try {
            JmsUtils.commitIfNecessary(session);
        }
        catch (JMSException e) {
            throw new ProvisioningException("Provisioning failed",
                JmsUtils.convertJmsAccessException(e));
        }
        finally {
            JmsUtils.closeSession(session);
            JmsUtils.closeConnection(connection);
        }
        return new JmsConsumerDestination(
            queues != null && queues.length > 0 ? queues[0] : null);
    }

    private Topic provisionTopic(String topicPattern, String topicName) {
        Connection activeMQConnection;
        Session session;
        Topic topic = null;
        try {
            activeMQConnection = connectionFactory.createConnection();
            session = activeMQConnection
                .createSession(true, Session.CLIENT_ACKNOWLEDGE);
            topic = session.createTopic(String.format(topicPattern, topicName));

            JmsUtils.commitIfNecessary(session);
            JmsUtils.closeSession(session);
            JmsUtils.closeConnection(activeMQConnection);
        }
        catch (JMSException e) {
            throw new IllegalStateException(e);
        }
        return topic;
    }

    private Queue[] provisionConsumerGroup(
        String consumerDestinationPattern,
        String topicName,
        String... consumerGroups) {

        Connection activeMQConnection;
        Session session;
        Queue[] groups = null;
        try {
            activeMQConnection = connectionFactory.createConnection();
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
                    groups[i] = createQueue(
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
        catch (JMSException e) {
            throw new IllegalStateException(e);
        }

        return groups;
    }

    private Queue createQueue(
        String destinationPattern,
        String topicName,
        Session session,
        String consumerGroup) throws JMSException {

        Queue queue = session.createQueue(
            String.format(destinationPattern, consumerGroup, topicName));
        //TODO: Understand why a producer is required to actually create the queue, it's not mentioned in ActiveMQ docs
        session.createProducer(queue).close();
        return queue;
    }
}

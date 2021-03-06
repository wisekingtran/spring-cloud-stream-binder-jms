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

package org.springframework.cloud.stream.binder.jms.activemq.config;

import javax.jms.ConnectionFactory;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.jms.JndiConnectionFactoryAutoConfiguration;
import org.springframework.boot.autoconfigure.jms.activemq.ActiveMQProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.stream.binder.jms.activemq.ActiveMQQueueProvisioner;
import org.springframework.cloud.stream.binder.jms.config.JmsBinderAutoConfiguration;
import org.springframework.cloud.stream.binder.jms.utils.DestinationNameResolver;
import org.springframework.cloud.stream.provisioning.ProvisioningProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * ActiveMQ specific configuration.
 * <p>
 * Creates the connection factory and the infrastructure provisioner.
 *
 */
@Configuration
@Import(JmsBinderAutoConfiguration.class)
@AutoConfigureAfter({ JndiConnectionFactoryAutoConfiguration.class })
@ConditionalOnClass({ ConnectionFactory.class,
    ActiveMQConnectionFactory.class })
@EnableConfigurationProperties({ ActiveMQProperties.class })
public class ActiveMQJmsConfiguration {

    @Bean
    ProvisioningProvider<?, ?> activeMqQueueProvisioner(
        final ConnectionFactory connectionFactory,
        final DestinationNameResolver destinationNameResolver) {

        return new ActiveMQQueueProvisioner(connectionFactory,
            destinationNameResolver);
    }

    @Bean
    public DestinationNameResolver queueNameResolver() throws Exception {
        return new DestinationNameResolver(new NullAnonymousNamingStrategy());
    }

}

/*
 *  Copyright 2017 the original author or authors.
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

package org.springframework.cloud.stream.binder.jms.provisioning;

import org.springframework.cloud.stream.provisioning.ProducerDestination;

/**
 * An implementation of {@link ProducerDestination} for JMS.
 *
 * @author Donovan Muller
 */
public class JmsProducerDestination implements ProducerDestination {

    private final String[] queueNames;

    public JmsProducerDestination(final String[] queueNames) {
        this.queueNames = queueNames;
    }

    
    public String[] getQueueNames() {
        return queueNames;
    }

    @Override
    public String getName() {

        return queueNames != null && queueNames.length > 0 ? queueNames[0]
                : null;
    }

    @Override
    public String getNameForPartition(int partition) {
        return queueNames != null && queueNames.length > 0 ? queueNames[0]
                : null;
    }

    @Override
    public String toString() {
        return "JmsProducerDestination{" + "partitionTopics=" + queueNames
                + '}';
    }
}

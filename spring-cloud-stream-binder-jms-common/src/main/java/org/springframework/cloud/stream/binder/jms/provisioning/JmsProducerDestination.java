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

import javax.jms.JMSException;
import javax.jms.Topic;

import org.springframework.cloud.stream.provisioning.ProducerDestination;

public class JmsProducerDestination implements ProducerDestination {

    private final Topic topic;

    public JmsProducerDestination(Topic topic) {
        this.topic = topic;
    }

    @Override
    public String getName() {

        try {
            return topic != null ? topic.getTopicName() : null;
        }
        catch (JMSException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public String getNameForPartition(int partition) {
        return getName();
    }

    @Override
    public String toString() {
        return "JmsProducerDestination{" + "partitionTopics=" + getName() + '}';
    }
}

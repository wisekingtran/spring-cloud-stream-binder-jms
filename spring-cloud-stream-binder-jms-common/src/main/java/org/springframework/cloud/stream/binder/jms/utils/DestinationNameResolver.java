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

package org.springframework.cloud.stream.binder.jms.utils;

import java.util.ArrayList;
import java.util.Collection;

import org.springframework.cloud.stream.binder.ConsumerProperties;
import org.springframework.cloud.stream.binder.ProducerProperties;
import org.springframework.util.StringUtils;

public class DestinationNameResolver {

    private final AnonymousNamingStrategy namingStrategy;

    public DestinationNameResolver(
            final AnonymousNamingStrategy namingStrategy) {
        this.namingStrategy = namingStrategy;
    }

    public String buildGroupName(
        final String group,
        final ConsumerProperties properties) {
        final boolean anonymous = !StringUtils.hasText(group);
        final String groupName = anonymous ? this.namingStrategy.generateName()
                : group;
        return properties.isPartitioned()
                ? this.buildName(properties.getInstanceIndex(), groupName)
                : groupName;
    }

    public Collection<DestinationNames> resolveDestinationNameForRequiredGroups(
        final String destinationName,
        final ProducerProperties properties) {

        final Collection<DestinationNames> output = new ArrayList<>(
            properties.getPartitionCount());
        if (properties.isPartitioned()) {

            // This is temporarily not supported. Leave this for next release
            final String[] requiredGroups = properties.getRequiredGroups();
            for (int index = 0; index < properties
                .getPartitionCount(); index++) {
                final String[] requiredPartitionGroupNames = new String[properties
                    .getRequiredGroups().length];
                for (int j = 0; j < requiredGroups.length; j++) {
                    requiredPartitionGroupNames[j] = this
                        .buildName(index, requiredGroups[j]);
                }
                final String topicName = this.buildName(index, destinationName);
                output.add(
                    new DestinationNames(topicName, requiredPartitionGroupNames,
                        index));
            }
        }
        else {
            output.add(
                new DestinationNames(destinationName,
                    properties.getRequiredGroups()));
        }

        return output;
    }

    private String buildName(final int index, final String group) {
        return String.format("%s-%s", group, index);
    }

}

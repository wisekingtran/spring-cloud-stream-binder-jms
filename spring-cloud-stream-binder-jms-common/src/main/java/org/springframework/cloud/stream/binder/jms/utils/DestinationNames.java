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

import java.util.Arrays;
import java.util.Objects;

public class DestinationNames {

    private final String destinationName;

    private final String[] groupNames;

    private final Integer partitionIndex;

    public DestinationNames(final String destinationName,
            final String[] groupNames) {
        this.destinationName = destinationName;
        this.groupNames = groupNames;
        this.partitionIndex = null;
    }

    public DestinationNames(final String destinationName,
            final String[] groupNames, final int partitionIndex) {
        this.destinationName = destinationName;
        this.groupNames = groupNames;
        this.partitionIndex = partitionIndex;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if ((o == null) || (this.getClass() != o.getClass())) {
            return false;
        }
        final DestinationNames that = (DestinationNames) o;
        return Objects.equals(this.destinationName, that.destinationName)
                && Arrays.equals(this.groupNames, that.groupNames)
                && Objects.equals(this.partitionIndex, that.partitionIndex);
    }

    public String getDestinationName() {
        return this.destinationName;
    }

    public String[] getGroupNames() {
        return this.groupNames;
    }

    public Integer getPartitionIndex() {
        return this.partitionIndex;
    }

    @Override
    public int hashCode() {
        return Objects
            .hash(this.destinationName, this.groupNames, this.partitionIndex);
    }

    @Override
    public String toString() {
        return "DestinationNames{" + "topicName='" + this.destinationName + '\''
                + ", groupNames=" + Arrays.toString(this.groupNames)
                + ", partitionIndex=" + this.partitionIndex + '}';
    }
}

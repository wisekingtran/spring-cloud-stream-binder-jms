package org.springframework.cloud.stream.binder.jms.config;

import org.apache.commons.lang.StringUtils;
import org.springframework.cloud.stream.binder.jms.utils.Constants;
import org.springframework.util.Assert;

public class JmsCommonProperties {

    private boolean bindQueueOnly;

    private String queuePattern = "Consumer.%s.VirtualTopic.%s";

    private String topicPattern = "VirtualTopic.%s";

    public String getQueuePattern() {
        return this.queuePattern;
    }

    public String getTopicPattern() {
        return this.topicPattern;
    }

    /**
     * By default, binding will be implemented using:<p>
     * <li>Producer: publishes message to a topic</li>
     * <li>Consumer: consumes message from a queue</li>
     * <p>
     *  When bindQueueOnly = false, the bindings will use queues only. It means producer(s) are created for each consumer queue.
     */
    public boolean isBindQueueOnly() {
        return this.bindQueueOnly;
    }

    public void setBindQueueOnly(final boolean bindQueueOnly) {
        this.bindQueueOnly = bindQueueOnly;
    }

    public void setQueuePattern(final String queuePattern) {
        if (queuePattern != null) {
            int matches = StringUtils
                .countMatches(queuePattern, Constants.PLACEHOLDER_STRING);
            Assert.isTrue(
                matches == 1 || matches == 2,
                "The queue pattern should contain only at most 2 placeholders (for destination and group)!");
        }
        this.queuePattern = queuePattern;
    }

    public void setTopicPattern(final String topicPattern) {
        this.topicPattern = topicPattern;
    }
}

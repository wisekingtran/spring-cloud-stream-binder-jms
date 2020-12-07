package org.springframework.cloud.stream.binder.jms.config;

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
        this.queuePattern = queuePattern;
    }

    public void setTopicPattern(final String topicPattern) {
        this.topicPattern = topicPattern;
    }
}

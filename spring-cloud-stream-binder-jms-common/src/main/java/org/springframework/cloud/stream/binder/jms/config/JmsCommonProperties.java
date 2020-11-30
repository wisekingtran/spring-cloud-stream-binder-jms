package org.springframework.cloud.stream.binder.jms.config;

public class JmsCommonProperties {

    /**
     * By default, binding will be implemented using:<p>
     * <li>Producer: publishes message to a topic</li>
     * <li>Consumer: consumes message from a queue</li>
     * <p>
     *  When bindQueueOnly = false, the bindings will use queues only. It means producer(s) are created for each consumer queue.
     */
    private boolean bindQueueOnly;

    public boolean isBindQueueOnly() {
        return bindQueueOnly;
    }

    public void setBindQueueOnly(boolean bindQueueOnly) {
        this.bindQueueOnly = bindQueueOnly;
    }

    private boolean groupEnabled;

    public boolean isGroupEnabled() {
        return groupEnabled;
    }

    public void setGroupEnabled(boolean groupEnabled) {
        this.groupEnabled = groupEnabled;
    }
}

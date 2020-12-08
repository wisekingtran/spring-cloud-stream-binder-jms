package org.springframework.cloud.stream.binder.jms.activemq.config;

import org.springframework.cloud.stream.binder.jms.utils.AnonymousNamingStrategy;

public class NullAnonymousNamingStrategy implements AnonymousNamingStrategy {

    @Override
    public String generateName() {
        return null;
    }

    @Override
    public String generateName(final String prefix) {
        return null;
    }

}

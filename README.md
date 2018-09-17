Spring Cloud Stream JMS Binder
The Spring Cloud Stream JMS Binder, provides a SCS binder based on JMS 1.1

There is a Pivotal Tracker® project publicly available.

Limitations
This binder is currently in alpha, bugs are expected.

JMS supports both point-to-point messaging using its Queue abstraction, and publish-subscribe messaging using Topic. However, neither of these patterns maps fully onto the SCS model of persistent publish-subscribe with consumer groups

JMS also does not support provisioning of queues and topics, whereas in SCS queues are created if required whenever they declared.

Some brokers which implement JMS nonetheless provide proprietary extensions which do support these required pieces of functionality. The Binder therefore delegates provisioning this to a broker-specific QueueProvisioner.

For more details, see the documentation for the individual broker support sub-modules.

Solace
ActiveMQ
IBM® MQ®
Provided implementations
Together with the root SPI the Spring Cloud Stream JMS module provides an implementation for:

Solace based on the Java proprietary Solace API.
ActiveMQ based on Virtual Destinations, a JMS compatible feature following certain naming conventions.
IBM® MQ® based on the Java proprietary libraries (PCF) provided with an IBM® MQ® installation.
Implementing new JMS providers
Before starting with a new JMS implementation, it is important to clarify that a Java compatible API should be provided by the vendor, enabling the provisioning of infrastructure at runtime. Additionally, SCS expects mixed topologies of topic-queues. i.e. Ideally, your vendor allows queues being subscribed to topics, so you can get groups of competing consumers (Queues) on top of topics. There might be other ways of achieving the same effect, e.g. through virtual topics as in ActiveMQ.

In order to create a new JMS provider, there are a some fundamental steps to be taken into account.

Implementing the SPI located at org.springframework.cloud.stream.binder.jms.spi.QueueProvisioner
Creating the configuration for your binder. Every JMS implementation is expected to provide at least a bean of type QueueProvider in addition to a reference to the root JMS configuration JMSAutoConfiguration an example of a working configuration class can be found in the Solace implementation in org.springframework.cloud.stream.binder.jms.solace.config.SolaceJmsConfiguration.
A spring.binders file under META-INF defining the configuration entry point for your binder SPI, probably the class created in the previous point.
It might be a good starting point checking out the existing implementations e.g. Solace or ActiveMQ.

Testing your new JMS provider
Apart from your unit tests, or integration tests, the JMS binder provides a template that will check most core features of the SCS model, verifying that your implementation is SCS compliant, in order to enable these tests you just need to implement org.springframework.cloud.stream.binder.test.integration.EndToEndIntegrationTests from the spring-cloud-stream-binder-jms-test-support maven submodule. e.g.:

public class MyEffortlessEndToEndIntegrationTests extends org.springframework.cloud.stream.binder.test.integration.EndToEndIntegrationTests {
    public EndToEndIntegrationTests() throws Exception {
        super(
                new MyBrandNewQueueProvisioner(),
                myJmsConnectionFactory
        );
    }
}
You might encounter an exception preventing the WebApplicationContext from being created, make sure to include a yaml (or alternatively properties) file in your test resources including, at least:

spring:
  main:
    web-environment: false

  # Necessary for org.springframework.cloud.stream.binder.jms.integration testing multiple ApplicationContexts.
  jmx:
    enabled: false
Depending on your technology it might be easier or more difficult to set up the infrastructure, if possible embedded, self-contained servers are preferred as in ActiveMQ, but this is not always possible as in Solace. Due to technical or legal reasons.
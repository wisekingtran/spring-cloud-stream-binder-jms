Spring Cloud Stream JMS Binder – ActiveMQ
-------------------------------------------------

This module provides provisioning functionality as required by [spring-cloud-stream-binder-jms](../../../).

### How it works

ActiveMQ allows creating the SCS model by using [virtual destinations](http://activemq.apache.org/virtual-destinations.html).
The effect is virtually the same as subscribing queues to topics.

The binder supports also the way to send messages directly to queue. This supports some simple scenarios to use very simple queue name like "processing-message". In this cases, it couldn't leverage the support from virtual destination.

##### CONSUMER MATRIX

| Group  | Queue Format | Queue Name | Topic Name |
| ------------- | ------------- |---|---|
| null | contains 2 %s | destination | |
| * | contains 2 %s | group.destination | |
| * | contains 1 %s | destination | |


##### PRODUCER MATRIX

| Group  | Queue Format | Queue Name | Topic Name |
| ------------- | ------------- |---|---|
| null | contains 2 %s | destination | |
| * | contains 2 %s | group.destination | |
| * | contains 1 %s | destination | |	 	 	 

### Compiling the module

The module provides out of the box everything required to be compiled.

### Running tests

Tests should run out of the box, and an embedded instance of ActiveMQ should
start in order to perform integration tests.

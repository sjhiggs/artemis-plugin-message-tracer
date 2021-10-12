
# Overview

Plugin to log message ids as a message traverses a cluster of brokers (or a single broker).  Useful to debug where messages become "stuck", "lost", etc.  This plugin only logs the Artemis message id and specified custom identifiers from message properties.  For example, if all messages in the system contain a 'breadcrumbId' that is set by producers, then this plugin can be configured to also log the breadcrumbId.

The intent is to log the minimal amount of information about messages and still be useful for tracking messages.  This plugin does not log all message headers, message bodies, etc.

# Example Mesages


## Broker A receives a message and sends the message to broker B via a store and forward queue
```
2021-10-04 12:53:05,158 AMQ991001: brokerA: exampleQueue: messageId=1075,breadcrumbId=744931,trackingId=568233: received by broker from /127.0.0.1:45708 (status OK)
2021-10-04 12:53:05,163 AMQ991000: brokerA: exampleQueue: messageId=1075,breadcrumbId=744931,trackingId=568233: sent to cluster at 127.0.0.1/127.0.0.1:61617 via queue $.artemis.internal.sf.my-cluster.c8d8e4cf-22c2-11ec-a155-201e8823caf8
```

## Broker B receives the message, sends it to a consumer, receives ACK back from consumer
```
2021-10-04 12:53:05,170 AMQ991001: brokerB: exampleQueue: messageId=1408,breadcrumbId=744931,trackingId=568233: received by broker from /127.0.0.1:33682 (status OK)
2021-10-04 12:53:05,172 AMQ991000: brokerB: exampleQueue: messageId=1408,breadcrumbId=744931,trackingId=568233: sent to consumer at /127.0.0.1:33688
2021-10-04 12:53:05,177 AMQ991002: brokerB: exampleQueue: messageId=1408,breadcrumbId=744931,trackingId=568233: ack received from consumer at /127.0.0.1:33688
```

# Logging configuration for separate file
```
loggers=org.eclipse.jetty,org.jboss.logging,org.apache.activemq.artemis.core.server,org.apache.activemq.artemis.utils,org.apache.activemq.artemis.utils.critical,org.apache.activemq.artemis.journal,org.apache.activemq.artemis.jms.server,org.apache.activemq.artemis.integration.bootstrap,org.apache.activemq.audit.base,org.apache.activemq.audit.message,org.apache.activemq.audit.resource


logger.org.apache.activemq.artemis.plugin.message.level=INFO
logger.org.apache.activemq.artemis.plugin.message.handlers=MESSAGE_TRACE_FILE
logger.org.apache.activemq.artemis.plugin.message.useParentHandlers=false


handler.MESSAGE_TRACE_FILE=org.jboss.logmanager.handlers.PeriodicRotatingFileHandler
handler.MESSAGE_TRACE_FILE.level=INFO
handler.MESSAGE_TRACE_FILE.properties=suffix,append,autoFlush,fileName
handler.MESSAGE_TRACE_FILE.suffix=.yyyy-MM-dd
handler.MESSAGE_TRACE_FILE.append=true
handler.MESSAGE_TRACE_FILE.autoFlush=true
handler.MESSAGE_TRACE_FILE.fileName=${artemis.instance}/log/messages.log
handler.MESSAGE_TRACE_FILE.formatter=MESSAGE_TRACE_PATTERN

formatter.MESSAGE_TRACE_PATTERN =org.jboss.logmanager.formatters.PatternFormatter
formatter.MESSAGE_TRACE_PATTERN.properties=pattern
formatter.MESSAGE_TRACE_PATTERN.pattern=%d %s%E%n
```

# Plugin Configuration

## Plugin Properties

* filters - comma delimited Java regex list for addresses that should not be logged.
* idNames - comma delimited list of custom identifiers that will be logged

## Example configuration

```
        <broker-plugin class-name="com.redhat.support.MessageAuditForHumans">
                <property key="filters" value="^activemq.*,^notif.*" />
                <property key="idNames" value="breadcrumbId,trackingId" />
        </broker-plugin>
```        

# Red Hat Openshift Installation/Example

One way to install the plugin on brokers running in openshift is to use an init container.  This guide will use Red Hat AMQ 7.x as the base image; the openshift environment already has a broker operator running and permissions are already granted to the Red Hat container registry to pull the base init container image.

## Install Broker

For demonstration purposes, a cluster of two brokers will be installed.  These commands are run within the project's 'openshift' directory.

```
$ oc new-project myamq
$ oc create -f test-broker/operator-group.yaml 
$ oc create -f test-broker/subscription.yaml 
$ ./build-custom-image.sh
$ oc create -f test-broker/broker.yaml
$ oc create -f test-broker/internal-service-broker-0.yaml 
$ oc create -f test-broker/internal-service-broker-1.yaml 
```


## Install test client

The test clients use a camel-k operator to create two camel-k integrations - a producer and consumer.  The producer connects to the broker- amqp service and the consumer connects to the broker-1 amqp service (previously defined).

```
$ oc create -f test-client/subscription.yaml
$ kamel run --property file:./test-client/amqp-producer.properties --dev ./test-client/Producer.java
$ kamel run --property file:./test-client/amqp-consumer.properties --dev ./test-client/Consumer.java
```

## Search for a message

```
$ ./trace-message.sh trackingid=68
```

Example output:
```
Name: broker-ss-0 HostIP: 10.0.94.35   PodIP: 10.131.0.168
Name: broker-ss-1 HostIP: 10.0.94.35   PodIP: 10.131.0.169

Name: consumer-887466784-gqqhf HostIP: 10.0.91.238   PodIP: 10.130.0.182
Name: producer-7bb7648c4d-n69cl HostIP: 10.0.91.238   PodIP: 10.130.0.181

./broker0.log:2021-10-12 AMQ991001: amq-broker: example: messageId=380,trackingid=68: received by broker from /10.130.0.181:38500 (status OK)
./broker0.log:2021-10-12 AMQ991000: amq-broker: example: messageId=524,trackingid=68: sent to cluster at broker-ss-1.broker-hdls-svc.myamq.svc.cluster.local/10.131.0.169:61616 via queue $.artemis.internal.sf.my-cluster.b0ec82c0-2b5f-11ec-bbcc-0a580a8300a9
./broker1.log:2021-10-12 AMQ991001: amq-broker: example: messageId=403,trackingid=68: received by broker from /10.131.0.168:42036 (status OK)
./broker1.log:2021-10-12 AMQ991000: amq-broker: example: messageId=403,trackingid=68: sent to consumer at /10.130.0.182:49300
./broker1.log:2021-10-12 AMQ991002: amq-broker: example: messageId=403,trackingid=68: ack received from consumer at /10.130.0.182:49300
```

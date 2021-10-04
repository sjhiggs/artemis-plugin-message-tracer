
# Overview

Plugin to log message ids as a message traverses a cluster of brokers (or a single broker).  Useful to debug where messages become "stuck", "lost", etc.

# Example Mesages


## Broker A receives a message and sends the message to broker B via a store and forward queue
```
2021-10-04 12:53:05,158 AMQ991001: brokerA: exampleQueue: messageId=1,075,breadcrumbId=744931,trackingId=568233: received by broker from /127.0.0.1:45708 (status OK)
2021-10-04 12:53:05,163 AMQ991000: brokerA: exampleQueue: messageId=1,075,breadcrumbId=744931,trackingId=568233: sent to cluster at 127.0.0.1/127.0.0.1:61617 via queue $.artemis.internal.sf.my-cluster.c8d8e4cf-22c2-11ec-a155-201e8823caf8
```

## Broker B receives the message, sends it to a consumer, receives ACK back from consumer
```
2021-10-04 12:53:05,170 AMQ991001: brokerB exampleQueue: messageId=1,408,breadcrumbId=744931,trackingId=568233: received by broker from /127.0.0.1:33682 (status OK)
2021-10-04 12:53:05,172 AMQ991000: brokerB: exampleQueue: messageId=1,408,breadcrumbId=744931,trackingId=568233: sent to consumer at /127.0.0.1:33688
2021-10-04 12:53:05,177 AMQ991002: brokerB: exampleQueue: messageId=1,408,breadcrumbId=744931,trackingId=568233: ack received from consumer at /127.0.0.1:33688
```



# Logging configuration
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

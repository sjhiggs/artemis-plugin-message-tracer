#!/bin/bash

echo "The config dir location is ${CONFIG_INSTANCE_DIR}..."

echo "Copying logging plugin to lib dir..."

cp /amq/lib/plugin-message-tracer-1.0.0.jar ${CONFIG_INSTANCE_DIR}/lib

echo "Adding logging plugin configuration to broker.xml..."

plugins="\n\n"
plugins="${plugins}      <broker-plugins>\n"
plugins="${plugins}       <broker-plugin class-name=\"org.apache.activemq.artemis.plugin.message.MessageTracer\">\n"
plugins="${plugins}               <property key=\"filters\" value=\"^activemq.*,^notif.*\" />"
plugins="${plugins}               <property key=\"idNames\" value=\"trackingid\" />"
plugins="${plugins}       </broker-plugin>"
plugins="${plugins}     </broker-plugins>"

sed -i "s|</addresses>|</addresses> $plugins|g" ${CONFIG_INSTANCE_DIR}/etc/broker.xml

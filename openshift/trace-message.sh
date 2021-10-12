#!/bin/bash

oc get pods --namespace=myamq --selector=ActiveMQArtemis --template='{{range .items}}Name: {{.metadata.name}} HostIP: {{.status.hostIP}}   PodIP: {{.status.podIP}}{{"\n"}}{{end}}{{"\n"}}'

oc get pods --namespace=myamq --selector=camel.apache.org/integration --template='{{range .items}}Name: {{.metadata.name}} HostIP: {{.status.hostIP}}   PodIP: {{.status.podIP}}{{"\n"}}{{end}}{{"\n"}}'


oc logs broker-ss-0 > broker0.log
oc logs broker-ss-1 > broker1.log

grep "AMQ99" ./broker*.log | grep "$1" | cut -d ' '  -f1,2,6-

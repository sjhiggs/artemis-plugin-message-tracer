#!/bin/bash

export IMG_NAME="amq-broker-init"
export PROJECT="myamq"

rm custom-init/lib/*.jar
cp ../target/plugin-message-tracer-*.jar custom-init/lib/

echo "Building custom init image using tag: ${IMG_NAME}"

oc new-build --binary --name=$IMG_NAME -l app=$IMG_NAME
oc patch bc/$IMG_NAME -p '{"spec":{"strategy":{"dockerStrategy":{"dockerfilePath":"Dockerfile"}}}}'
oc start-build $IMG_NAME --from-dir=custom-init --follow

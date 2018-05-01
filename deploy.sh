#!/bin/bash

# Exit on any error
set -e

for f in k8s/*.yml
do
	envsubst < $f > "generated-$(basename $f)"
done
sudo /opt/google-cloud-sdk/bin/gcloud docker -- push gcr.io/${PROJECT_NAME}/puppeteer:$CIRCLE_SHA1
sudo chown -R ubuntu:ubuntu /home/ubuntu/.kube
kubectl apply -f generated-deployment.yml

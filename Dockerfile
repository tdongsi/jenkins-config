FROM jenkins/jenkins:lts
MAINTAINER dongsi.tuecuong@gmail.com

# Over provisioning flags for Kubernetes plugin.
# By default, Jenkins spawns agents conservatively.
# Reference: https://github.com/jenkinsci/kubernetes-plugin#over-provisioning-flags
# Do not change these values unless you know what you are doing.
# Without them, jobs will wait in queue.
# There are two paradigms to CI builds and resource allocation:
# A. optimize for resource utilization
# B. optimize for job throughput
# We optimize A on the Kubernetes level and B on the Jenkins level, so we provision jenkins nodes aggressively
ENV JAVA_OPTS "-Dhudson.model.LoadStatistics.decay=0.2 -Dhudson.slaves.NodeProvisioner.initialDelay=0 -Dhudson.slaves.NodeProvisioner.MARGIN=50 -Dhudson.slaves.NodeProvisioner.MARGIN0=0.85"

COPY plugins.txt /usr/share/jenkins/ref/plugins.txt
RUN /usr/local/bin/install-plugins.sh < /usr/share/jenkins/ref/plugins.txt

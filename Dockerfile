FROM cdongsi/jenkins:test
MAINTAINER dongsi.tuecuong@gmail.com

USER root
RUN ln -s /opt/jdk-latest/bin/jar /usr/local/bin/jar

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

USER jenkins

COPY jenkins-support /usr/local/bin/jenkins-support
COPY install-plugins.sh /usr/local/bin/install-plugins.sh
COPY plugins.txt /usr/share/jenkins/ref/plugins.txt
RUN /usr/local/bin/install-plugins.sh < /usr/share/jenkins/ref/plugins.txt

COPY init_scripts/src/main/groovy/scripts/Auth.groovy /usr/share/jenkins/ref/init.groovy.d/Auth.groovy

VOLUME /var/jenkins_home/code

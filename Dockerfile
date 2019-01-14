FROM jenkins/jenkins:2.138.2
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
ENV JAVA_OPTS "-Djenkins.install.runSetupWizard=false -Dhudson.model.LoadStatistics.decay=0.2 -Dhudson.slaves.NodeProvisioner.initialDelay=0 -Dhudson.slaves.NodeProvisioner.MARGIN=50 -Dhudson.slaves.NodeProvisioner.MARGIN0=0.85"

USER jenkins

COPY jenkins-support /usr/local/bin/jenkins-support
COPY install-plugins.sh /usr/local/bin/install-plugins.sh
COPY plugins.txt /usr/share/jenkins/ref/plugins.txt
RUN /usr/local/bin/install-plugins.sh < /usr/share/jenkins/ref/plugins.txt

# Create SSH key
RUN mkdir -p ${HOME}/.ssh &&\
    ssh-keygen -f ${HOME}/.ssh/id_rsa -t rsa -b 4096 -N ''

VOLUME /var/jenkins_home/code

# Configs that make sense in local and production environment
COPY src/main/groovy/System.groovy /usr/share/jenkins/ref/init.groovy.d/System.groovy

# The following configs are only applicable in local development
COPY src/main/groovy/Auth.groovy /usr/share/jenkins/ref/init.groovy.d/Auth.groovy
COPY src/main/groovy/Credentials.groovy /usr/share/jenkins/ref/init.groovy.d/Credentials.groovy
COPY src/main/groovy/DevFolder.groovy /usr/share/jenkins/ref/init.groovy.d/DevFolder.groovy

# Secrets are not checked-in
# Be explicit on what is being used.
COPY secrets/minikube.pfx /usr/share/jenkins/ref/secret_data/minikube.pfx
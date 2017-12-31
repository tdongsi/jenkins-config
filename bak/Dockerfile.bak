FROM maven:3.5-jdk-8
MAINTAINER dongsi.tuecuong@gmail.com

ENV DEBIAN_FRONTEND=noninteractive
RUN apt-get update && apt-get install -y git curl zip && rm -rf /var/lib/apt/lists/*

ARG JENKINS_VERSION=2.30
ENV JENKINS_HOME /var/jenkins_home
ENV JENKINS_SLAVE_AGENT_PORT 50000

# Over provisioning flags for Kubernetes plugin.
# By default, Jenkins spawns agents conservatively.
# Reference: https://github.com/jenkinsci/kubernetes-plugin#over-provisioning-flags
# Do not change these values unless you know what you are doing.
# Without them, jobs will wait in queue.
# There are two paradigms to CI builds and resource allocation:
# A. optimize for resource utilization
# B. optimize for job throughput
# We optimize A on the Kubernetes level and B on the Jenkins level, so we provision jenkins nodes aggressively
ENV JAVA_OPTS "-Dhudson.model.LoadStatistics.decay=0.2 -Dhudson.slaves.NodeProvisioner.MARGIN=50 -Dhudson.slaves.NodeProvisioner.MARGIN0=0.85"

ARG user=jenkins
ARG group=jenkins
ARG uid=1000
ARG gid=1000

# Jenkins is run with user `jenkins`, uid = 1000
# If you bind mount a volume from the host or a data container,
# ensure you use the same uid
RUN groupadd -g ${gid} ${group} \
    && useradd -d "$JENKINS_HOME" -u ${uid} -g ${gid} -m -s /bin/bash ${user}

# Jenkins home directory is a volume, so configuration and build history
# can be persisted and survive image upgrades
VOLUME /var/jenkins_home

# `/usr/share/jenkins/ref/` contains all reference configuration we want
# to set on a fresh new installation. Use it to bundle additional plugins
# or config file with your custom jenkins Docker image.
RUN mkdir -p /usr/share/jenkins/ref/init.groovy.d

COPY init.groovy /usr/share/jenkins/ref/init.groovy.d/tcp-slave-agent-port.groovy

RUN apt-get update && apt-get install -y tini jenkins=$JENKINS_VERSION && rm -rf /var/lib/apt/lists/* /var/cache/apt/*

ENV JENKINS_UC https://updates.jenkins.io
RUN chown -R ${user} "$JENKINS_HOME" /usr/share/jenkins/ref

# for main web interface:
EXPOSE 8080

# will be used by attached slave agents:
EXPOSE 50000

ENV COPY_REFERENCE_FILE_LOG $JENKINS_HOME/copy_reference_file.log

USER ${user}

COPY jenkins.sh /usr/local/bin/jenkins.sh
ENTRYPOINT ["/usr/local/bin/tini", "--", "/usr/local/bin/jenkins.sh"]
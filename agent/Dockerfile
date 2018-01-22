FROM maven:3.5-jdk-8
MAINTAINER dongsi.tuecuong@gmail.com
LABEL Description="This image provides the Jenkins agent executable (slave.jar)" Vendor="Jenkins project" Version="3.10"

USER root
ENV HOME /home/jenkins
RUN groupadd -g 10000 jenkins &&\
    useradd -c "Jenkins user" -d $HOME -u 10000 -g 10000 -m jenkins

# Files in docker folder is extracted from docker-17.03.0-ce.tgz
# NOTE: Avoid keeping tgz file around
COPY docker /usr/local/docker

RUN groupadd -g 1001 docker &&\
    usermod -G docker jenkins &&\
    ln -s /usr/local/docker/docker /usr/bin/docker

# Install Jenkins
ARG VERSION=3.10
ARG AGENT_WORKDIR=/home/jenkins/agent

RUN curl --create-dirs -sSLo /usr/share/jenkins/slave.jar https://repo.jenkins-ci.org/public/org/jenkins-ci/main/remoting/${VERSION}/remoting-${VERSION}.jar \
  && chmod 755 /usr/share/jenkins \
  && chmod 644 /usr/share/jenkins/slave.jar

# Install Debian packages
RUN apt-get update &&\
    apt-get install -y python-pip

# Install Python packages
RUN pip install --upgrade pip &&\
    pip install requests

USER jenkins
ENV AGENT_WORKDIR=${AGENT_WORKDIR}
RUN mkdir /home/jenkins/.jenkins && mkdir -p ${AGENT_WORKDIR}

VOLUME /home/jenkins/.jenkins
VOLUME ${AGENT_WORKDIR}
WORKDIR /home/jenkins

COPY jenkins-slave /usr/local/bin/jenkins-slave

ENTRYPOINT ["jenkins-slave"]

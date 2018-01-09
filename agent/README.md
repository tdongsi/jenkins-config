# jenkins-agent Docker image

This Docker image provides the Jenkins agent for running with Kubernetes plugin.

### Build and usage

```text
docker build -t cdongsi/jenkins-agent:latest .
```

A few tools can be pre-installed in the Docker image to fit my personal development need.
For these tools, detailed instructions are in the following sections.
Files for these additional tools are expected in the same folder with `Dockerfile`.

NOTE: In production, depending on the context, you might be better off to have smaller Docker images with fewer tools pre-installed.
For example, you may have `python-agent` image that only has Python-related binaries and `java-agent` image that only has Java-related binaries.
However, for local development, it is more *convenient* to have all tools available.

#### Docker

Docker is needed in Jenkins jobs that build Docker images.
Docker binaries are added into Jenkins agent image by the following section of code:

```dockerfile
# Files in docker folder is extracted from docker-17.03.0-ce.tgz
# NOTE: Avoid keeping tgz file around
COPY docker /usr/local/docker

RUN groupadd -g 1001 docker &&\
    usermod -G docker jenkins &&\
    ln -s /usr/local/docker/docker /usr/bin/docker
```

`docker` folder contains Docker binaries, extracted from `docker-17.03.0-ce.tgz` which is downloaded from Docker website.
  
NOTE: We build Docker images from a Jenkins agent which is another Docker container in this setup (Jenkins master and agents are containers in Kubernetes).
There are two paradigms for building Docker images in a Docker container: Docker-in-Docker (DIND) and Docker-out-of-Docker (DOOD) ([more discussion](http://tdongsi.github.io/blog/2017/04/23/docker-out-of-docker/)).
The general recommendation is that DOOD is better fit for CI systems, including [DIND creator himself in this blog post](https://jpetazzo.github.io/2015/09/03/do-not-use-docker-in-docker-for-ci/).

### Testing the Docker image

To verify the Docker image working, configure Kubernetes plugin to add a Pod template with correct image tag and assign 
an appropriate label to it (e.g., `java-agent`).

Add a Jenkinsfile for a test job such as follows:

```groovy
// Test library setup
library 'jenkins-shared-library@master'

node('java-agent') {
    stage('Checkout') {
        checkout scm
    }
    
    stage('Main') {
        // Test Python setup
        sh(script: 'python -c "import requests"', returnStatus: true)
        // Test Docker setup
        sh 'docker version'
    }
    
    stage('Post') {
        // Print info of standard tools
        sh 'ls -al'
        sh 'java -version'
        sh 'mvn -version'
        sh 'python -V'
    }
}
```

### Troubleshooting

Running `docker version` on the Jenkins agent may give you the following error message:

```text
+ docker version
Client:
 Version:      17.03.0-ce
 API version:  1.26
 Go version:   go1.7.5
 Git commit:   3a232c8
 Built:        Tue Feb 28 07:52:04 2017
 OS/Arch:      linux/amd64
Got permission denied while trying to connect to the Docker daemon socket at unix:///var/run/docker.sock: 
Get http://%2Fvar%2Frun%2Fdocker.sock/v1.26/version: dial unix /var/run/docker.sock: connect: permission denied
```

The above problem is likely due to `docker` group is not set with the correct group ID in Jenkins agent's Docker image.
Modify the Dockerfile to fix it, nothing that the group ID in `groupadd` command must match the `docker` group in minikube VM.

```dockerfile
RUN groupadd -g 1001 docker &&\
    usermod -G docker jenkins &&\
    ln -s /usr/local/docker/docker /usr/bin/docker
```

Find the value of group ID by using the following commands:

```text
tdongsi$ minikube ssh
                         _             _
            _         _ ( )           ( )
  ___ ___  (_)  ___  (_)| |/')  _   _ | |_      __
/' _ ` _ `\| |/' _ `\| || , <  ( ) ( )| '_`\  /'__`\
| ( ) ( ) || || ( ) || || |\`\ | (_) || |_) )(  ___/
(_) (_) (_)(_)(_) (_)(_)(_) (_)`\___/'(_,__/'`\____)

$ sudo cat /etc/group
...
docker:x:1001:
...

```


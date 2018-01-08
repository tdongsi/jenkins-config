# jenkins-agent Docker image

This Docker image provides the Jenkins agent for running with Kubernetes plugin.

### Build and usage

A few tools are pre-installed in the Docker image to fit my need.
For these tools, the followings are expected in the same folder with `Dockerfile`.

NOTE: In production, depending on the context, you might be better off to have smaller Docker images with fewer tools pre-installed.
For example, you may have `python-agent` image that only has Python-related binaries and `java-agent` image that only has Java-related binaries.
However, for local development, it is more *convenient* to have all tools available.

#### Docker

* `docker` folder contains Docker binaries, extracted from `docker-17.03.0-ce.tgz` which is downloaded from Docker website.
  * `docker` binaries are needed in Jenkins jobs that build Docker images.
  
NOTE2: We build Docker images from a Jenkins agent which is another Docker container in this setup (Jenkins master and agents are containers in Kubernetes).
There are two paradigms for building Docker images in a Docker container: Docker-in-Docker (DIND) and Docker-out-of-Docker (DOOD).
IMO, I would choose DOOD for CI systems, agreeing with [DIND creator himself in this blog post](https://jpetazzo.github.io/2015/09/03/do-not-use-docker-in-docker-for-ci/).

### Testing the Docker image

TODO

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


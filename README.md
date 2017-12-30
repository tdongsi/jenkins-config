# jenkins-config

Jenkins config as code for local development.

### How to build and run

```
docker build -t myjenkins:latest .
mkdir jdata
docker run -p 8080:8080 -p 50000:50000 -v jdata:/var/jenkins_home myjenkins

docker run --rm --name jenkins-local -v mavenrepo:/root/.m2 \
-v ~/Matrix/jenkins-shared-library:/var/jenkins_home/pipeline-library \
-v ~/Matrix/jenkins-global-library:/var/jenkins_home/pipeline-libs \
-e DEV_HOST=127.0.0.1 -p 8080:8080 -p 50000:50000 myjenkins:latest
```

**Reference**:

* [Base Docker image](https://hub.docker.com/r/jenkins/jenkins/)
* [Usage instruction](https://hub.docker.com/_/jenkins/). Note that the image is deprecated.

### Installing plugins

Construct the list of plugins and versions you want to use in the file `plugins.txt`.
It is recommended to keep the plugin in alphabetical order for easy record keeping and code review.
For an existing Jenkins instance, you can obtain its list of installed plugins by running the following Groovy script in its Script Console (accessed via `Manage Jenkins` > `Script Console`):

``` groovy Get list of installed plugins
Jenkins.instance.pluginManager.plugins.sort { it.shortName }.each {
  plugin -> 
    println "${plugin.shortName}:${plugin.version}"
}
```

Other methods can be found in [this Stackoverflow thread](https://stackoverflow.com/questions/9815273/how-to-get-a-list-of-installed-jenkins-plugins-with-name-and-version-pair).

#### Kubernetes plugin

For Kubernetes plugin, we have to add provisioning flags, based on [its recommendation](https://github.com/jenkinsci/kubernetes-plugin#over-provisioning-flags).
By default, Jenkins spawns agents conservatively. 
Say, if there are 2 builds in queue, it won't spawn 2 executors immediately. 
It will spawn one executor and wait for sometime for the first executor to be freed before deciding to spawn the second executor.
If you want to override this behaviour and spawn an executor for each build in queue immediately without waiting, you can use these flags during Jenkins startup:

```
-Dhudson.slaves.NodeProvisioner.initialDelay=0
-Dhudson.slaves.NodeProvisioner.MARGIN=50
-Dhudson.slaves.NodeProvisioner.MARGIN0=0.85
```

See [here](https://support.cloudbees.com/hc/en-us/articles/115000060512-New-agents-are-not-being-provisioned-for-my-jobs-in-the-queue-when-I-have-agents-that-are-suspended) for meaning of the provision flags above.

### Local Kubernetes

#### Installing Minikube
Install `kubectl` and `minikube`, both from [kubernetes on Github](https://github.com/kubernetes/).

```plain Install minikube
tdongsi$ curl -Lo minikube https://storage.googleapis.com/minikube/releases/v0.24.1/minikube-darwin-amd64 &&\
 chmod +x minikube && mv minikube /usr/local/bin/
  % Total    % Received % Xferd  Average Speed   Time    Time     Time  Current
                                 Dload  Upload   Total   Spent    Left  Speed
100 39.3M  100 39.3M    0     0  5401k      0  0:00:07  0:00:07 --:--:-- 7069k

tdongsi$ minikube start
Starting local Kubernetes v1.8.0 cluster...
Starting VM...
Downloading Minikube ISO
 140.01 MB / 140.01 MB [============================================] 100.00% 0s
Getting VM IP address...
Moving files into cluster...
Downloading localkube binary
 148.25 MB / 148.25 MB [============================================] 100.00% 0s
 0 B / 65 B [----------------------------------------------------------]   0.00%
 65 B / 65 B [======================================================] 100.00% 0sSetting up certs...
Connecting to cluster...
Setting up kubeconfig...
Starting cluster components...
Kubectl is now configured to use the cluster.
Loading cached images from config file.

tdongsi$ kubectl version
Client Version: version.Info{Major:"1", Minor:"5", GitVersion:"v1.5.4", GitCommit:"7243c69eb523aa4377bce883e7c0dd76b84709a1", 
GitTreeState:"clean", BuildDate:"2017-03-07T23:53:09Z", GoVersion:"go1.7.4", Compiler:"gc", Platform:"darwin/amd64"}
Server Version: version.Info{Major:"1", Minor:"8", GitVersion:"v1.8.0", GitCommit:"0b9efaeb34a2fc51ff8e4d34ad9bc6375459c4a4", 
GitTreeState:"clean", BuildDate:"2017-11-29T22:43:34Z", GoVersion:"go1.9.1", Compiler:"gc", Platform:"linux/amd64"}
```

Some good to know commands for minikube:

```text
tdongsi$ minikube dashboard --url
http://192.168.99.100:30000

tdongsi$ minikube service <your_service> --url
```

#### Troubleshooting: Minikube and VPN

If you are connected to corporate VPN, you might have problem with starting Minikube.

```
tdongsi-ltm4:jenkins-dev tdongsi$ minikube start
Starting local Kubernetes v1.8.0 cluster...
Starting VM...
Downloading Minikube ISO
 140.01 MB / 140.01 MB [============================================] 100.00% 0s

^C
```

I have attempted different approaches for this issue, but none are consistently working.

1. Use OpenConnect for VPN access rather than Cisco's AnyConnect client.
1. Set port forwarding for the minikube VM to forward port 8443 on 127.0.0.1 to port 8443 in the VM.
1. Use `--host-only-cidr` option in `minikube start`.

In addition, [this pull request](https://github.com/kubernetes/minikube/pull/1329) supposedly fixes the issue, in v0.19.0 release.

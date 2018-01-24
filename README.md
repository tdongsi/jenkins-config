# jenkins-config

Jenkins config as code for local development of Groovy-based Pipeline libraries.

### How to build and run

```
docker build -t cdongsi/jenkins:latest .

docker run -p 8080:8080 -p 50000:50000 -v /data/mydata:/var/jenkins_home \
-v /Users/tdongsi/Mycode:/var/jenkins_home/code cdongsi/jenkins:latest
```

**Reference**:

* [Base Docker image](https://hub.docker.com/r/jenkins/jenkins/)
* [Usage instruction](https://hub.docker.com/_/jenkins/). Note that the image is deprecated.

#### Jenkins configuration

TODO: Groovy hooks.

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

Other methods to get similar list can be found in [this Stackoverflow thread](https://stackoverflow.com/questions/9815273/how-to-get-a-list-of-installed-jenkins-plugins-with-name-and-version-pair).
The `install-plugins.sh` script and supporting files are already in latest Jenkins's Docker image.
Otherwise, they can be obtained from [this Github](https://github.com/jenkinsci/docker).

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

tdongsi$ minikube start --disk-size=50g --memory 4096 --kubernetes-version=v1.8.0
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

##### Troubleshooting: Minikube and VPN

If you are connected to corporate VPN, you might have problem with starting Minikube.

```
tdongsi$ minikube start --disk-size=50g --memory 4096 --kubernetes-version=v1.8.0
Starting local Kubernetes v1.8.0 cluster...
Starting VM...
Downloading Minikube ISO
 140.01 MB / 140.01 MB [============================================] 100.00% 0s

^C
```

In that case you may have to set up port forwarding for minikube VM.
See [this blog post](http://tdongsi.github.io/blog/2017/12/31/minikube-in-corporate-vpn/) for full discussions.

#### Tips and Tricks

Some good-to-know commands for minikube:

```text
# This will get k8s dashboard URL
tdongsi$ minikube dashboard --url
http://192.168.99.100:30000

tdongsi$ minikube service <your_service> --url

# This will allow minikube to reuse local Docker image without uploading
tdongsi$ eval $(minikube docker-env)
# The Docker client/context is now switched to minikube's Docker daemon
tdongsi$ docker images
REPOSITORY                                             TAG                 IMAGE ID            CREATED             SIZE
gcr.io/google_containers/kubernetes-dashboard-amd64    v1.8.0              55dbc28356f2        4 weeks ago         119MB
gcr.io/k8s-minikube/storage-provisioner                v1.8.0              4689081edb10        7 weeks ago         80.8MB
gcr.io/k8s-minikube/storage-provisioner                v1.8.1              4689081edb10        7 weeks ago         80.8MB
gcr.io/google_containers/k8s-dns-sidecar-amd64         1.14.5              fed89e8b4248        3 months ago        41.8MB
gcr.io/google_containers/k8s-dns-kube-dns-amd64        1.14.5              512cd7425a73        3 months ago        49.4MB
gcr.io/google_containers/k8s-dns-dnsmasq-nanny-amd64   1.14.5              459944ce8cc4        3 months ago        41.4MB
gcr.io/google_containers/kubernetes-dashboard-amd64    v1.6.3              691a82db1ecd        5 months ago        139MB
gcr.io/google-containers/kube-addon-manager            v6.4-beta.2         0a951668696f        6 months ago        79.2MB
gcr.io/google_containers/pause-amd64                   3.0                 99e59f495ffa        20 months ago       747kB
```

If you have problems with minikube’s Docker daemon building your images, you can also copy the image from your local daemon into minikube like this: 
`docker save <image> | minikube ssh docker load` 
(currently not working due to [this bug](https://github.com/kubernetes/minikube/issues/1957)).

The alternative is to save and load manually:

```text
tdongsi$ docker save myjenkins:0.1 -o myjenkins.tar

tdongsi$ scp -i $(minikube ssh-key) myjenkins.tar docker@$(minikube ip):/home/docker

tdongsi$ minikube ssh "docker load --input /home/docker/myjenkins.tar"
a85f35566a26: Loading layer [==================================================>]  196.8MB/196.8MB
...
```

#### Deploy Jenkins

Use the pre-defined YAML file.

```text
tdongsi$ kubectl create -f k8s/jenkins.yaml
deployment "jenkins" created
service "jenkins" created
tdongsi$ kubectl get pods
NAME                       READY     STATUS    RESTARTS   AGE
jenkins-7874759567-5pbwv   1/1       Running   0          2s

tdongsi$ minikube service jenkins --url
http://192.168.99.100:30080
```

Note that the volumes are specifically defined in `jenkins.yaml` to map `JENKINS_HOME` to `/data/mydata`
and `JENKINS_HOME/code` to `/Users/tdongsi/Mycode`.
Such mapping is NOT coincidental and one should NOT modify such setup unless he understands what he's doing.
Specifically, `/data/...` is chosen for `JENKINS_HOME` since it is one of few [persistent folders in Minikube](https://kubernetes.io/docs/getting-started-guides/minikube/#persistent-volumes). 
`/Users/tdongsi/...` is chosen since it is the [only mounted host folder for OSX](https://kubernetes.io/docs/getting-started-guides/minikube/#mounted-host-folders).
These are not configurable at the moment and different for the driver and OS you are using.

**Troubleshooting**:

You may get the following errors:

```text
tdongsi$ kubectl get pods
NAME                       READY     STATUS             RESTARTS   AGE
jenkins-7874759567-jqkww   0/1       CrashLoopBackOff   1          23s

tdongsi$ kubectl logs jenkins-7874759567-jqkww
touch: cannot touch ‘/var/jenkins_home/copy_reference_file.log’: Permission denied
Can not write to /var/jenkins_home/copy_reference_file.log. Wrong volume permissions?
```

If your hostPath is `/your/home`, it will store the jenkins data in `/your/home` on the host. 
Ensure that `/your/home` is accessible by the `jenkins` user in container (uid 1000) or use `-u some_other_user` parameter with `docker run`. 
To fix it, you must set the correct permissions in the host before you mount volumes.

```text
tdongsi$ minikube ssh sudo chown 1000 /data/mydata
```

Note that since `JENKINS_HOME` is intentionally persistent in the default setup, remember to clear the `/data` folder or 
change volume mapping when working on Docker image.


#### Configure Kubernetes plugin

The Minikube client certificate needs to be converted to PKCS, will need a password

```text
tdongsi$ openssl pkcs12 -export -out secrets/minikube.pfx -inkey ~/.minikube/apiserver.key -in ~/.minikube/apiserver.crt -certfile ~/.minikube/ca.crt -passout pass:secret
```

Validate that the certificates work

```text
tdongsi$ curl --cacert ~/.minikube/ca.crt --cert secrets/minikube.pfx:secret https://192.168.99.100:8443
{
  "paths": [
    "/api",
    "/api/v1",
    "/apis",
    "/apis/",
    "/apis/admissionregistration.k8s.io",
    ...
}
```

Add a Jenkins credential of type "Certificate", upload it from `~/.minikube/minikube.pfx`, password `secret`.

* Kubernetes URL: `https://192.168.99.100:8443` where `192.168.99.100` is output of `minikube ip` and `https://`(or `http://` or different port number depending on setup) is required.  
* Fill *Kubernetes server certificate key* with the contents of `~/.minikube/ca.crt`, after removing `-----BEGIN CERTIFICATE-----`
 and `-----END CERTIFICATE-----` lines.
* Disable https certificate check: Check if you use `http` in Kubernetes URL.
* Credentials: Use the one that you just created with `minikube.pfx` file.
* Use *Test Connection* button to verify your Kubernetes configuration.
* Jenkins URL: Use the Cluster IP from `kubectl get service jenkins` command output.

To verify if the plugin is configured correctly and the Kubernetes backend is functional, configure a pod template for slave agents and create a simple test job.
See [this](agent/README.md) for how to build a slave agent's Docker image and an example job.

#### Update configuration in live Jenkins instance

The default configurations of Jenkins are defined in Groovy hook scripts as explained above.
It helps removing many manual steps whenever starting a new local Jenkins instance for testing.
However, once you already deploy Jenkins into Minikube and start developing, it is best to keep that Jenkins instance running and persist any current jobs or changes.
The volumes defined in `jenkins.yaml` file is intended to be persistent in Minikube and all the changes are saved automatically.

However, on the occasions that changes in the Groovy hook scripts are required, please update the Groovy scripts and the live Jenkins instance. 
The easiest way to update the Jenkins instance is to run the updated Groovy commands in Script Console (access via Mange Jenkins > Script Console). 

### Configure shared pipeline libraries

Jenkins shared libraries is the most common and preferred ways to share commonly used Groovy codes in Jenkinsfile.
Jenkins systems in production may use different combinations of Jenkins shared libraries.
[This presentation](https://www.youtube.com/watch?v=M8U9RyL756U) is one of the most thorough reviews of different ways of deploying shared libraries in Jenkins.

For developing Jenkinsfile or shared libraries, we would want to mimic the configurations of shared Jenkins libraries in production.
For that, we should consider configuring the following Jenkins shared libraries in the following particular order:
 
1. Configure Global Pipeline Libraries for a custom Mock Step Library, such as [this setup](https://github.com/tdongsi/jenkins-steps-override/blob/master/README.md).
  This is for overriding built-in Pipeline steps such as `mail` or `sendSlack` in local development.
2. Create symlink to mimic [internally hosted shared library at workflowLibs](http://tdongsi.github.io/blog/2017/03/17/jenkins-pipeline-shared-libraries/): 
   `ln -s /var/jenkins_home/code/internal-global-library/vars /var/jenkins_home/workflow-libs/vars`.
3. Finally, configure Pipeline Libraries at the Folder level for the remaining Jenkins shared libraries.
   
Note that using retrieval option with "Legacy SCM > File System" only allows one location at a time.
If your Pipeline Libraries in production comes from more than one source (e.g., two Github repositories), then you have to configure the remaining with similar retrieval option (e.g., Modern SCM > Github).
For verifying correctness of library configurations, you can use the same test Jenkinsfile in [this folder](agent/README.md).

### Setup IntelliJ IDEA for local development

TODO

### References

* [Working with Groovy Init Scripts](https://www.bonusbits.com/wiki/HowTo:Setup_Project_in_IntellJ_IDEA_for_Working_with_Jenkins_Plugins_Groovy_Init_Scripts)
* [Working with Pipeline libraries](https://st-g.de/2016/08/jenkins-pipeline-autocompletion-in-intellij)
* [Different setups of Jenkins shared libraries](https://www.youtube.com/watch?v=M8U9RyL756U)

### Related projects

* [Jenkins agent](agent/README.md) including test Jenkinsfile.
* [Mock Pipeline Steps library](https://github.com/tdongsi/jenkins-steps-override)
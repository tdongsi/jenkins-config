# Jenkins configuration as code

### Overview

For local development and troubleshooting, we want to mimic production Jenkins as close as possible
while minimizing manual configuration steps to get there.
For that purpose, we use Jenkins's `install-plugin.sh` script to install the exactly same set of plugins used in production Jenkins. 
We also use various [Groovy Hook Scripts](https://wiki.jenkins.io/display/JENKINS/Groovy+Hook+Script) in [the Docker image](../Dockerfile) for configuring Jenkins master into similar settings as production Jenkins. 
This can help us to easily spin up a running Jenkins instance that have similar settings to production Jenkins.
See [this blog post](http://tdongsi.github.io/blog/2017/12/30/groovy-hook-script-and-jenkins-configuration-as-code/) for more details such as purposes of various Groovy codes.

### Setting up IntelliJ for Groovy script development

See [IDEA setup](IDE.md).

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

### Special notes on Kubernetes plugin

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

#### Configure Kubernetes plugin in Minikube

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
  * If `kube-dns` is already installed and running, you should use `<service-name>.<namespace-name>.svc.cluster.local`, such as `jenkins.sfdc.svc.cluster.local`.

To verify if the plugin is configured correctly and the Kubernetes backend is functional, configure a pod template for slave agents and create a simple test job.
See [this](agent/README.md) for how to build a slave agent's Docker image and an example job.

### Update configuration in live Jenkins instance

The default configurations of Jenkins are defined in Groovy hook scripts as explained above.
It helps removing many manual steps whenever starting a new local Jenkins instance for testing.
However, once you already deploy Jenkins into Minikube and start developing, it is best to keep that Jenkins instance running and persist any current jobs or changes.
The volumes defined in `jenkins.yaml` file is intended to be persistent in Minikube and all the changes are saved automatically.

However, on the occasions that changes in the Groovy hook scripts are required, please update the Groovy scripts and the live Jenkins instance. 
The easiest way to update the Jenkins instance is to run the updated Groovy commands in Script Console (access via Mange Jenkins > Script Console). 
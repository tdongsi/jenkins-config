# jenkins-config
Jenkins config as code

### How to build and run

```
docker build -t myjenkins:latest .
mkdir jdata
docker run -p 8080:8080 -p 50000:50000 -v jdata:/var/jenkins_home myjenkins
```

**Reference**:

* [Base Docker image](https://hub.docker.com/r/jenkins/jenkins/)
* [Usage instruction](https://hub.docker.com/_/jenkins/). Note that the image is deprecated.

### Installing plugins

Construct the list of plugins and versions you want to use in the file `plugins.txt`.
It is recommended to keep the plugin in alphabetical order for easy record keeping and code review.
For an existing Jenkins instance, you can obtain its list of installed plugins by running the following Groovy script in its Script Console:

``` groovy Get list of installed plugins
Jenkins.instance.pluginManager.plugins.sort { it.shortName }.each {
  plugin -> 
    println "${plugin.shortName}:${plugin.version}"
}
```

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

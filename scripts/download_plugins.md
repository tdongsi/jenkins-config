
Script to install Jenkins plugins including dependencies while Jenkins is offline
Cloned from [here](https://gist.github.com/chuxau/6bc42f0f271704cd4e91) and modified for my needs.

**Usage**:

* Create a file listing the plugins and the required versions in the format below. Supply that file's name as your first parameter when you run the script.
* The destination for the plugins to be downloaded should be the second parameter.

Format of the plugin list file: *plugin:version*. For example:

``` 
ace-editor:1.1
analysis-core:1.95
ant:1.8
antisamy-markup-formatter:1.5
apache-httpcomponents-client-4-api:4.5.5-3.0
authentication-tokens:1.3
blueocean:1.6.2
blueocean-autofavorite:1.2.2
blueocean-bitbucket-pipeline:1.6.2
blueocean-commons:1.6.2
blueocean-config:1.6.2
blueocean-core-js:1.6.2
blueocean-dashboard:1.6.2
blueocean-display-url:2.2.0
blueocean-events:1.6.2
blueocean-git-pipeline:1.6.2
blueocean-github-pipeline:1.6.2
blueocean-i18n:1.6.2
blueocean-jira:1.6.2
blueocean-jwt:1.6.2
```

**Notes from other users**:

* The last line in the plugin list file is ignored, the workaround is simply to end your plugin list file with a blank line.
* Note that the `set -e` breaks dep resolution in the event that a plugin without deps ends up first in the plugins directory because the unzip pipe fails. You can either turn off `set -e`, or add `||true` to the end of the unzip pipe.
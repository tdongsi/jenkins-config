/*
This script obtains credentials ID with username and password.

def creds = com.cloudbees.plugins.credentials.CredentialsProvider.lookupCredentials(
    com.cloudbees.plugins.credentials.common.StandardUsernameCredentials.class,
    Jenkins.instance,
    null,
    null
);
for (c in creds) {
  if (c instanceof com.cloudbees.plugins.credentials.common.StandardUsernamePasswordCredentials) {
    println "${c.id}:${c.username}:${c.password}"
  }
}
*/

// Use https://github.com/jenkinsci/credentials-plugin to figure out package
import com.cloudbees.plugins.credentials.impl.UsernamePasswordCredentialsImpl
import org.jenkinsci.plugins.plaincredentials.impl.StringCredentialsImpl
import com.cloudbees.plugins.credentials.domains.Domain
import com.cloudbees.plugins.credentials.CredentialsScope
import hudson.util.Secret

// Based on https://youtu.be/RzWkn_ENVcc?t=2263
def domain = Domain.global()
def store = Jenkins.instance.getExtensionList('com.cloudbees.plugins.credentials.SystemCredentialsProvider')[0].getStore()

def githubAccount = new UsernamePasswordCredentialsImpl(
        CredentialsScope.GLOBAL, "test-github", "Test Github Account",
        'testuser', // If using k8s secret: new File('/mnt/secrets/github/username').text,
        'testpassword', // If using k8s secret: new File('/mnt/secrets/github/token').text
)

def secretString = new StringCredentialsImpl(
        CredentialsScope.GLOBAL, "test-secret-string", "Test Secret String",
        Secret.fromString('testpassword') // If using k8s secret: Secret.fromString(new File('/mnt/secrets/github/token').text)
)

store.addCredentials(domain, githubAccount)
store.addCredentials(domain, secretString)
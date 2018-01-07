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


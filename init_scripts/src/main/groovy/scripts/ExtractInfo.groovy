/**
 * Created by tdongsi on 2/7/18.
 */

// This script obtains credentials ID with username and password.
import com.cloudbees.plugins.credentials.CredentialsProvider
import com.cloudbees.plugins.credentials.common.StandardUsernamePasswordCredentials
def creds = CredentialsProvider.lookupCredentials(StandardUsernamePasswordCredentials.class, Jenkins.instance, null, null)
for (c in creds) {
    if (c instanceof StandardUsernamePasswordCredentials) {
        println "${c.id}:${c.username}:${c.password}"
    }
}


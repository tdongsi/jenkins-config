/**
 * Created by tdongsi on 2/7/18.
 */

import com.cloudbees.plugins.credentials.CredentialsProvider
import com.cloudbees.plugins.credentials.common.StandardUsernamePasswordCredentials
import groovy.json.JsonBuilder
import jenkins.model.Jenkins
import org.jenkinsci.plugins.plaincredentials.StringCredentials
import org.jenkinsci.plugins.plaincredentials.FileCredentials

import java.nio.charset.StandardCharsets

println("=== List of plugins ===")
Jenkins.instance.pluginManager.plugins.sort { it.shortName }.each {
    plugin ->
        println "${plugin.shortName}:${plugin.version}"
}

println("=== Username with Password ===")
class UsernamePassword {
    String id
    String description
    String username
    String password
}

def createUserPassFile() {
    def myList = []
    def json = new JsonBuilder()

    def creds = CredentialsProvider.lookupCredentials(
            StandardUsernamePasswordCredentials.class, Jenkins.instance, null, null
    ).sort{ it.id }
    for (c in creds) {
        if (c instanceof StandardUsernamePasswordCredentials) {
            // println "${c.id}:${c.username}:${c.password}"
            def o = new UsernamePassword(id: c.id, description: c.description, username: c.username, password: c.password)
            myList.add(o)
        }
    }
    json userpass: myList
    json.toPrettyString()
}

println("=== Secret text ===")
class SecretText {
    String id
    String description
    String secret
}

def createSecretText() {
    def myList = []
    def json = new JsonBuilder()
    def creds = CredentialsProvider.lookupCredentials(StringCredentials.class, Jenkins.instance, null, null).sort{ it.id }
    for (c in creds) {
        if (c instanceof StringCredentials) {
            // println "${c.id}:${c.secret.plainText}"
            def o = new SecretText(id: c.id, description: c.description, secret: c.secret.plainText)
            myList.add(o)
        }
    }
    json secretText: myList
    json.toPrettyString()
}

println("=== Secret file ===")
class SecretFile {
    String id
    String description
    String filename
    String content
}
def createSecretFile() {
    // Binary file mapping
    String SECRET_BASE = '/var/jenkins_home/secret_data/'
    def secretFileMapping = [
            "ajna_p12": SECRET_BASE + 'ajna.p12',
            "app.emp.client.jks": SECRET_BASE + 'app.emp.client.jks',
            'aqueduct-keystore': SECRET_BASE + 'app.emp.client.jks',
            'p12cert': SECRET_BASE + 'hostcert-chain.p12',
            'hostcert-chain-p12': SECRET_BASE + 'hostcert-chain.p12',
            'ib-hbase': SECRET_BASE + 'processing.keytab',
            'hbase': SECRET_BASE + 'common.keytab',
            'iot-keytab': SECRET_BASE + 'common.keytab', // NOT USED
            'iot-ajna-jks': SECRET_BASE + 'iot-dva-kafka-prd-test.jks',
            'iot-entrichment-keytab': SECRET_BASE + 'enrichment.keytab'
    ]

    def myList = []
    def json = new JsonBuilder()

    def creds = CredentialsProvider.lookupCredentials(FileCredentials.class, Jenkins.instance, null, null).sort{ it.id }
    for (c in creds) {
        if (c instanceof FileCredentials) {
            if ( !secretFileMapping.containsKey(c.id) ) {
                // Skip the binary files, only process text file.
                byte[] bytes = new byte[c.content.available()]
                c.content.read(bytes)
                String fileText = new String(bytes, StandardCharsets.UTF_8)
                // println "${c.id}:${fileText}"

                def o = new SecretFile(id: c.id, description: c.description, filename: c.fileName, content: fileText)
                myList.add(o)
            }
        }
    }

    json secretFile: myList
    json.toPrettyString()
}

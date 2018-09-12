/**
 * Created by tdongsi on 2/4/18.
 */

import com.cloudbees.plugins.credentials.impl.UsernamePasswordCredentialsImpl
import groovy.json.JsonSlurper
import org.jenkinsci.plugins.plaincredentials.impl.StringCredentialsImpl
import org.jenkinsci.plugins.plaincredentials.impl.FileCredentialsImpl
import com.cloudbees.plugins.credentials.domains.Domain
import com.cloudbees.plugins.credentials.CredentialsScope
import jenkins.model.Jenkins
import hudson.util.Secret
import com.cloudbees.jenkins.plugins.sshcredentials.impl.BasicSSHUserPrivateKey
import com.cloudbees.plugins.credentials.impl.CertificateCredentialsImpl
import com.cloudbees.plugins.credentials.SecretBytes

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

def domain = Domain.global()
def store = Jenkins.instance.getExtensionList('com.cloudbees.plugins.credentials.SystemCredentialsProvider')[0].getStore()
String SECRET_BASE = "/var/jenkins_home/code/jenkins-config/secrets/"
def json = new JsonSlurper()

// ******** "Username with Password" Type
def USERPASS_FILE = Paths.get(SECRET_BASE, "userpass.txt")
def creds = json.parse(USERPASS_FILE.toFile())
for (cred in creds.userpass) {
    def userpass = new UsernamePasswordCredentialsImpl(
            CredentialsScope.GLOBAL,
            cred["id"],
            cred["description"],
            cred["username"],
            cred["password"]
    )
    // TODO: Overwrite if the credentials id exists?
    store.addCredentials(domain, userpass)
}

// ******** "Secret Text" Type
def STRING_FILE = Paths.get(SECRET_BASE, "string.txt")
creds = json.parse(STRING_FILE.toFile())
for (cred in creds["string"]) {
    def secretString = new StringCredentialsImpl(
            CredentialsScope.GLOBAL,
            cred["id"],
            cred["description"],
            Secret.fromString(cred["secret"])
    )
    store.addCredentials(domain, secretString)
}

// ******** "Username with Private Key" Type
String keyfile = "/var/jenkins_home/.ssh/id_rsa"
def privateKey = new BasicSSHUserPrivateKey(
        CredentialsScope.GLOBAL,
        "jenkins_ssh_key",
        "git",
        new BasicSSHUserPrivateKey.FileOnMasterPrivateKeySource(keyfile),
        "",
        ""
)
store.addCredentials(domain, privateKey)

// ******** "Certificate" Type

String minikubeKeyfile = "/var/jenkins_home/secret_data/minikube.pfx"
def minikubeCreds = new CertificateCredentialsImpl(
        CredentialsScope.GLOBAL,
        "minikube",
        "Minikube client certificate",
        "secret",
        new CertificateCredentialsImpl.FileOnMasterKeyStoreSource(minikubeKeyfile))
store.addCredentials(domain, minikubeCreds)

// ******** "Secret File" Type
// Text files
def TEXTFILE_FILE = Paths.get(SECRET_BASE, "textfile.txt")
creds = json.parse(TEXTFILE_FILE.toFile())
for (cred in creds["textfile"]) {
    SecretBytes secretBytes = SecretBytes.fromBytes(cred["content"].getBytes())
    def secretFile = new FileCredentialsImpl(
            CredentialsScope.GLOBAL,
            cred["id"],
            cred["description"],
            cred["filename"],
            secretBytes
    )
    store.addCredentials(domain, secretFile)
}

// Binary files. E.g.: Kerberos keytab, certificate.
// NOTE: Should have used Paths.get() instead
def secretFileMapping = [
        'p12cert': SECRET_BASE + 'hostcert-chain.p12',
        'hostcert-chain-p12': SECRET_BASE + 'hostcert-chain.p12',
        'hbase': SECRET_BASE + 'common.keytab',
]

for (key in secretFileMapping.keySet()) {
    Path file = Paths.get(secretFileMapping[key])
    SecretBytes secretBytes = SecretBytes.fromBytes(Files.readAllBytes(file))
    def secretFile = new FileCredentialsImpl(
            CredentialsScope.GLOBAL,
            key,
            key,
            file.fileName.toString(),
            secretBytes
    )
    store.addCredentials(domain, secretFile)
}

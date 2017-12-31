import hudson.security.HudsonPrivateSecurityRealm
import hudson.security.GlobalMatrixAuthorizationStrategy
import jenkins.model.Jenkins
import hudson.model.*

println("=== Installing the Security Realm")
def instance = Jenkins.getInstance()

def securityRealm = new HudsonPrivateSecurityRealm(false)
User user = securityRealm.createAccount("user", "ranger1")
user.setFullName("FirstRanger")
User admin = securityRealm.createAccount("admin", "time4morphin")
admin.setFullName("PowerRanger")
instance.setSecurityRealm(securityRealm)

// Make sure matrix-auth plugin is installed. Check plugins.txt file.
def strategy = new GlobalMatrixAuthorizationStrategy()

strategy.add(Jenkins.ADMINISTER, 'authenticated')

instance.setAuthorizationStrategy(strategy)

instance.save()
import hudson.security.HudsonPrivateSecurityRealm
import hudson.security.GlobalMatrixAuthorizationStrategy
import hudson.security.FullControlOnceLoggedInAuthorizationStrategy
import jenkins.model.Jenkins
import hudson.model.*
import com.cloudbees.plugins.credentials.CredentialsProvider
import hudson.PluginManager
import org.jenkinsci.plugins.workflow.cps.replay.ReplayAction

println("=== Installing the Security Realm")
def instance = Jenkins.getInstance()

def securityRealm = new HudsonPrivateSecurityRealm(false)
User user = securityRealm.createAccount("user", "ranger1")
user.setFullName("FirstRanger")
User admin = securityRealm.createAccount("admin", "time4morphin")
admin.setFullName("PowerRanger")
instance.setSecurityRealm(securityRealm)

boolean isGranularityNeeded = true

if ( !isGranularityNeeded ) {

    // Easy way first: No granularity needed
    // "Logged in user can do anything" strategy in GUI
    def strategy = new FullControlOnceLoggedInAuthorizationStrategy()
    strategy.setAllowAnonymousRead(false)
    instance.setAuthorizationStrategy(strategy)

} else {
    // NOTE: Make sure matrix-auth plugin is installed. Check plugins.txt file.
    // This can help reproduce certain user's permission
    // "Matrix Authorization" strategy in GUI
    def strategy = new GlobalMatrixAuthorizationStrategy()

    String USER = 'authenticated' // or 'admin'

    //  Slave Permissions
    strategy.add(hudson.model.Computer.BUILD,USER)
    strategy.add(hudson.model.Computer.CONFIGURE,USER)
    strategy.add(hudson.model.Computer.CONNECT,USER)
    strategy.add(hudson.model.Computer.CREATE,USER)
    strategy.add(hudson.model.Computer.DELETE,USER)
    strategy.add(hudson.model.Computer.DISCONNECT,USER)

    //  Credential Permissions
    strategy.add(com.cloudbees.plugins.credentials.CredentialsProvider.CREATE,USER)
    strategy.add(com.cloudbees.plugins.credentials.CredentialsProvider.DELETE,USER)
    strategy.add(com.cloudbees.plugins.credentials.CredentialsProvider.MANAGE_DOMAINS,USER)
    strategy.add(com.cloudbees.plugins.credentials.CredentialsProvider.UPDATE,USER)
    strategy.add(com.cloudbees.plugins.credentials.CredentialsProvider.VIEW,USER)

    //  Overall Permissions
    strategy.add(hudson.model.Hudson.ADMINISTER,USER)
    strategy.add(hudson.PluginManager.CONFIGURE_UPDATECENTER,USER)
    strategy.add(hudson.model.Hudson.READ,USER)
    strategy.add(hudson.model.Hudson.RUN_SCRIPTS,USER)
    strategy.add(hudson.PluginManager.UPLOAD_PLUGINS,USER)

    //  Job Permissions
    strategy.add(hudson.model.Item.BUILD,USER)
    strategy.add(hudson.model.Item.CANCEL,USER)
    strategy.add(hudson.model.Item.CONFIGURE,USER)
    strategy.add(hudson.model.Item.CREATE,USER)
    strategy.add(hudson.model.Item.DELETE,USER)
    strategy.add(hudson.model.Item.DISCOVER,USER)
    strategy.add(hudson.model.Item.READ,USER)
    strategy.add(hudson.model.Item.WORKSPACE,USER)

    //  Run Permissions
    strategy.add(hudson.model.Run.DELETE,USER)
    strategy.add(org.jenkinsci.plugins.workflow.cps.replay.ReplayAction.REPLAY,USER)
    strategy.add(hudson.model.Run.UPDATE,USER)

    //  View Permissions
    strategy.add(hudson.model.View.CONFIGURE,USER)
    strategy.add(hudson.model.View.CREATE,USER)
    strategy.add(hudson.model.View.DELETE,USER)
    strategy.add(hudson.model.View.READ,USER)

    instance.setAuthorizationStrategy(strategy)
}


instance.save()
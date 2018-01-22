import hudson.security.csrf.DefaultCrumbIssuer
import jenkins.model.Jenkins
import jenkins.model.JenkinsLocationConfiguration
import jenkins.security.s2m.AdminWhitelistRule
import org.kohsuke.stapler.StaplerProxy
import hudson.tasks.Mailer

println("-- Basic Jenkins security")
println("--- Configuring Remoting (JNLP4 only, no Remoting CLI)")
// NOTE: this only works with Jenkins 2.46.2 and later
// Jenkins.instance.getDescriptor("jenkins.CLI").get().setEnabled(false)
// TODO: Use "JNLP4-connect" after updating Jenkins version
Jenkins.instance.agentProtocols = new HashSet<String>(["JNLP2-connect", "Ping"])

println("--- Enable Slave -> Master Access Control")
Jenkins.instance.getExtensionList(StaplerProxy.class)
    .get(AdminWhitelistRule.class)
    .masterKillSwitch = false

println("--- Checking the CSRF protection")
if (Jenkins.instance.crumbIssuer == null) {
    println "CSRF protection is disabled, Enabling the default Crumb Issuer"
    Jenkins.instance.crumbIssuer = new DefaultCrumbIssuer(true)
}

println("--- Configuring Quiet Period")
// We do not wait for anything
Jenkins.instance.quietPeriod = 0

println("--- Set Maven Project Configuration")
Jenkins.instance.setNumExecutors(1)
Jenkins.instance.setMode(Node.Mode.EXCLUSIVE)

println("--- Set fixed SSHD port")
def sshDesc = Jenkins.instance.getDescriptor("org.jenkinsci.main.modules.sshd.SSHD")
sshDesc.setPort(12222)
sshDesc.save()

// Save them all
Jenkins.instance.save()

println("--- Configuring Email global settings")
JenkinsLocationConfiguration.get().adminAddress = "admin@non.existent.email"
Mailer.descriptor().defaultSuffix = "@non.existent.email"

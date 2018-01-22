// Initializes the Development folder, which is fully configurable by the user

import groovy.io.FileType
import hudson.plugins.filesystem_scm.FSSCM
import jenkins.model.Jenkins
import com.cloudbees.hudson.plugins.folder.Folder
import org.jenkinsci.plugins.workflow.cps.CpsFlowDefinition
import org.jenkinsci.plugins.workflow.cps.CpsScmFlowDefinition
import org.jenkinsci.plugins.workflow.job.WorkflowJob
import org.jenkinsci.plugins.workflow.libs.FolderLibraries
import org.jenkinsci.plugins.workflow.libs.LibraryConfiguration
import org.jenkinsci.plugins.workflow.libs.SCMRetriever

println("=== Initialize the Development folder")
if (Jenkins.instance.getItem("Development") != null) {
    println("Development folder has been already initialized, skipping the step")
    return
}

// Admin owns the root Development folder
def folder = Jenkins.instance.createProject(Folder.class, "Development")
// FolderOwnershipHelper.setOwnership(folder, new OwnershipDescription(true, "admin"))

// Create a library for local Jenkins Pipeline Library Development
println("==== Initializing local Pipeline Library development dir")
String libraryPath = '/var/jenkins_home/code/jenkins-shared-library'
File file = new File("${libraryPath}/vars")
if (!file.exists()) {
    println("${libraryPath} is not found, skipping")
    return
} else {
    println("${libraryPath} is found, initializing the directory")
}

def pipelineLib = folder.createProject(Folder.class, "jenkins-shared-library")
// FolderOwnershipHelper.setOwnership(pipelineLib, new OwnershipDescription(true, "user"))
def scm = new FSSCM(libraryPath, false, false, null)
LibraryConfiguration lc = new LibraryConfiguration("jenkins-shared-library", new SCMRetriever(scm))
lc.with {
    implicit = true
    defaultVersion = "master"
}
pipelineLib.addProperty(new FolderLibraries([lc]))

// Add extra Pipeline libs
def customLib = folder.createProject(Folder.class, "CustomLibraries")
def customPipelineLibs = [lc]
def pipelineLibsDir = new File("/var/jenkins_home/pipeline-libs")
if (pipelineLibsDir.exists()) {
    println("===== Adding local Pipeline libs")
    pipelineLibsDir.eachFile (FileType.DIRECTORIES) { customLibPath ->
        if (new File(customLibPath, ".Jenkinslib").exists()) {
            println("===== Adding ${customLibPath}")
            def customLibScm = new FSSCM("${customLibPath}", false, false, null)
            LibraryConfiguration customLibConfig = new LibraryConfiguration("${customLibPath.name}", new SCMRetriever(customLibScm))
            customLibConfig.with {
                implicit = true
                defaultVersion = "master"
            }
            customPipelineLibs.add(customLibConfig)
        }
    }
}
customLib.addProperty(new FolderLibraries(customPipelineLibs))

// Add sample projects
static def createPipelineLibJob(Folder folder, String repo, String nameSuffix = "", String args = null) {
    WorkflowJob sshdModuleProject = folder.createProject(WorkflowJob.class, "${repo}${nameSuffix}")
    String extras = args == null ? "" : ", $args"
    sshdModuleProject.definition = new CpsFlowDefinition(
        "buildPlugin(platforms: ['linux'], repo: 'https://github.com/jenkinsci/${repo}.git' ${extras})", true
    )
}

createPipelineLibJob(pipelineLib, "job-restrictions-plugin", "_findbugs", "findbugs: [archive: true, unstableTotalAll: '0']")
createPipelineLibJob(pipelineLib, "sshd-module")
createPipelineLibJob(pipelineLib, "sshd-module", "_findbugs", "findbugs: [archive: true, unstableTotalAll: '0']")
createPipelineLibJob(pipelineLib, "sshd-module", "_findbugs_checkstyle", "findbugs: [archive: true, unstableTotalAll: '0'], checkstyle: [run: true, archive: true]")
// Just a plugin, where FindBugs really fails
createPipelineLibJob(pipelineLib, "last-changes-plugin", "_findbugs", "findbugs: [archive: true, unstableTotalAll: '0']")

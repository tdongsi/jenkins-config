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
def pipelineLib = Jenkins.instance.createProject(Folder.class, "Development")
// FolderOwnershipHelper.setOwnership(pipelineLib, new OwnershipDescription(true, "user"))

// Create a library for local Jenkins Pipeline Library Development
println("==== Initializing local Pipeline Library development dir")
String CODE_BASE = '/var/jenkins_home/code/'
String libraryRepo = 'jenkins-shared-library'
String libraryPath = "${CODE_BASE}${libraryRepo}"
File file = new File("${libraryPath}/vars")
if (!file.exists()) {
    println("${libraryPath} is not found, skipping")
    return
} else {
    println("${libraryPath} is found, initializing the directory")
}


def scm = new FSSCM(libraryPath, false, false, null)
LibraryConfiguration lc = new LibraryConfiguration(libraryRepo, new SCMRetriever(scm))
lc.with {
    implicit = true
    defaultVersion = "master"
}
pipelineLib.addProperty(new FolderLibraries([lc]))

// Add extra Pipeline libs
CODE_BASE = '/var/jenkins_home/code/'
def pipelineDir = new File(CODE_BASE)
if (pipelineDir.exists()) {
    println("== Adding local pipelines")
    pipelineDir.eachFile (FileType.DIRECTORIES) { myPath ->
        def jenkinsFile = new File(myPath, "Jenkinsfile")
        if (jenkinsFile.exists()) {
            String pipelineName = myPath.getName()
            println("===== Adding ${pipelineName}")

            WorkflowJob job = folder.createProject(WorkflowJob.class, pipelineName)
            job.definition = new CpsFlowDefinition(jenkinsFile.text, false) // Do not use sandbox
        }
    }
}

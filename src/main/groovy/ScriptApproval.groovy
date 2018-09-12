/**
 * Created by tdongsi on 2/13/18.
 */

import org.jenkinsci.plugins.scriptsecurity.scripts.ScriptApproval
import org.jenkinsci.plugins.scriptsecurity.scripts.ApprovalContext

ScriptApproval sa = ScriptApproval.get()

def all = """
new groovy.json.JsonSlurperClassic
method groovy.json.JsonSlurperClassic parseText java.lang.String
"""

def signature = "new groovy.json.JsonSlurperClassic"
ScriptApproval.PendingSignature s = new ScriptApproval.PendingSignature(signature, false, ApprovalContext.create())
sa.getPendingSignatures().add(s)

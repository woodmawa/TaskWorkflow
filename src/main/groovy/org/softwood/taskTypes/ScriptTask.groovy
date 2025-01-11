package org.softwood.taskTypes

import groovy.transform.ToString
import groovy.util.logging.Slf4j
import org.codehaus.groovy.control.MultipleCompilationErrorsException
import org.softwood.taskTypes.secureScriptBase.SecureScriptEvaluator

import java.util.concurrent.CompletableFuture

@Slf4j
@ToString (includeNames=true, includes = ["taskName", "taskType", "taskCategory", "status", "startTime", "endTime"])
class ScriptTask implements ExecutableTaskTrait  {

    String taskType = this.class.getSimpleName()
    TaskCategories taskCategory = TaskCategories.Task
    Closure secureTaskScript = {/* no op*/}

    String copyOfScriptText
    RuntimeException scriptException

    //@Autowired (false) WorkflowExecutionContext taskExecutionContext

    ScriptTask (String scriptText = null) {
        taskWork = ScriptTask::runTask
    }


    private void internalSetSecureScript (Closure script) {
        if (script)
            secureTaskScript = script
    }

    void setScript (String scriptText) {
        def task = this
        if (scriptText) {
            Closure script = new SecureScriptEvaluator().parse(scriptText, task )
            secureTaskScript = internalSetSecureScript (script)
        }

    }

    //returns completeable future
    private def runTask(Map variables=[:]) {

        taskResult = new CompletableFuture<>()

        //task result is future of computed outcome or the exception itself
        taskResult.completeAsync {
            try {
                log.debug "Script's runTask:  running taskScript and get its Future result "
                secureTaskScript.call()        //call the secure task script closure ...
            } catch (SecurityException | MultipleCompilationErrorsException | Exception ex) {
                log.info "script task $taskName threw exception  : " + ex.stackTrace
                this.status = TaskStatus.EXCEPTION
                scriptException = ex
                return ex
            }
        }

    }


}

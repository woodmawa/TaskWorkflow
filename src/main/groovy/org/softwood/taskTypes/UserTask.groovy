package org.softwood.taskTypes

import com.sun.net.httpserver.Authenticator
import groovy.transform.ToString
import groovy.util.logging.Slf4j
import org.codehaus.groovy.control.MultipleCompilationErrorsException
import org.softwood.taskTypes.secureScriptBase.SecureScriptEvaluator

import java.util.concurrent.CompletableFuture

@Slf4j
@ToString (includeNames=true, includes = ["taskName", "taskType", "taskCategory", "status", "startTime", "endTime"])
class UserTask implements ExecutableTaskTrait {

    String taskType = this.class.getSimpleName()
    TaskCategories taskCategory = TaskCategories.Task
    Exception userException
    enum UserTaskOutcome  {
        APPROVED,
        REJECTED,
        CANCELLED
    }


    //@Autowired (false) WorkflowExecutionContext taskExecutionContext

    UserTask () {
        taskWork = UserTask::runTask
    }

    //returns completeable future
    private def runTask(Map variables=[:]) {

        taskResult = new CompletableFuture<>()

        //task result is future of computed outcome or the exception itself
        taskResult.completeAsync {
            try {
                log.debug "Script's runTask:  running taskScript and get its Future result "
            } catch (SecurityException | MultipleCompilationErrorsException | Exception ex) {
                log.info "user task $taskName threw exception  : " + ex.stackTrace
                this.status = TaskStatus.EXCEPTION
                userException = ex
                return ex
            }
        }

    }
}
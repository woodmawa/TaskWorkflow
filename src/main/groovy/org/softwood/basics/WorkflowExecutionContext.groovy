package org.softwood.basics

import java.time.LocalDateTime

interface WorkflowExecutionContext {
    WorkflowExecutionContext getExecutionContext()
    String getProcessInstanceName ()
    LocalDateTime getStartTime()
    LocalDateTime getEndTime()
    WorkflowExecutionContext start ()
    WorkflowExecutionContext start (Map inputVariables)
    void stop ()
    void setProcessVariables (Map variables)
    Map getProcessVariables ()

}
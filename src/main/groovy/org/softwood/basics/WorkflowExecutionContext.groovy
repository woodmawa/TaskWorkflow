package org.softwood.basics

import java.time.LocalDateTime

interface WorkflowExecutionContext {
    WorkflowExecutionContext getExecutionContext()
    String getProcessInstanceName ()
    LocalDateTime getStartTime()
    LocalDateTime getEndTime()
    WorkflowExecutionContext start ()
    void stop ()

}
package org.softwood.basics

import groovy.util.logging.Slf4j

import java.time.LocalDateTime

@Slf4j
class WorkflowExecutionContextImpl implements WorkflowExecutionContext {

    private String processInstanceId = UUID.randomUUID().toString()
    private final String processInstanceName
    private LocalDateTime startTime, endTime
    private String processDefinitionTemplate

    //constructor - needs an instance name
    WorkflowExecutionContextImpl (String name, String template) {
        processInstanceName = name
        processDefinitionTemplate = template
    }

    @Override
    WorkflowExecutionContext start () {
        startTime = LocalDateTime.now()

        log.info "starting process $processInstanceId (name:$processInstanceName) from template $processDefinitionTemplate"
        //todo
        //create first task from template and queue that
        //for execution
        this
    }

    @Override
    void stop () {
        log.info "ending process $processInstanceId "
        endTime = LocalDateTime.now()
    }

    @Override
    WorkflowExecutionContext getExecutionContext() {
        return this
    }

    @Override
    String getProcessInstanceName() {
        return processInstanceName
    }

    @Override
    LocalDateTime getStartTime() {
        return null
    }

    @Override
    LocalDateTime getEndTime() {
        return null
    }
}

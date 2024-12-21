package org.softwood.processEngine

import groovy.util.logging.Slf4j
import org.softwood.processLibrary.ProcessTemplateLibrary
import org.softwood.processLibrary.StandardProcessTemplateInstance
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

import java.beans.PropertyChangeEvent
import java.beans.PropertyChangeListener
import java.beans.PropertyChangeSupport
import java.time.LocalDateTime

@Component
@Slf4j
class ProcessRuntime {
    enum RuntimeStatus {
        Uninitialised,
        Running,
        Shutdown
    }
    LocalDateTime started = LocalDateTime.now()
    private RuntimeStatus status = RuntimeStatus.Uninitialised
    final List<PropertyChangeListener> runtimeListeners =[]

    private PropertyChangeSupport support


    ProcessRuntime () {
        support = new PropertyChangeSupport(this)
        status = RuntimeStatus.Running
    }

    void addRuntimeListener (PropertyChangeListener listener) {
        support.addPropertyChangeListener(listener)
    }

    void removeRuntimeListener (PropertyChangeListener listener) {
        support.removePropertyChangeListener (listener)
    }

    void setStatus(RuntimeStatus newStatus) {
        RuntimeStatus oldStatus = this.status
        this.status = newStatus
        support.firePropertyChange("status", oldStatus, newStatus)
    }

    RuntimeStatus getStatus () {
        status
    }

    void shutdown () {
        setStatus (RuntimeStatus.Shutdown)
        support.firePropertyChange(new PropertyChangeEvent("status", status, RuntimeStatus.Shutdown))
    }

    @Autowired
    ProcessTemplateLibrary processLibrary

    ProcessInstance startProcess (String processTemplateName) {

        ProcessInstance processInstance

        if (!processTemplateName) {
            throw new IllegalArgumentException("Process template name cannot be null or empty")
        }

        processInstance = createProcessInstance(processTemplateName)
        log.info("Started process with template: ${processTemplateName}")
        return processInstance

    }

    private ProcessInstance createProcessInstance(String processTemplateName) {
        switch (processTemplateName.toLowerCase()) {
            case 'standard':
                return StandardProcessTemplateInstance.helloWorldProcess2()
            default:
                // TODO: Implement process library search
                // return processLibrary.search(processTemplateName)
                throw new UnsupportedOperationException("Process template not supported: ${processTemplateName}")
        }
    }
}


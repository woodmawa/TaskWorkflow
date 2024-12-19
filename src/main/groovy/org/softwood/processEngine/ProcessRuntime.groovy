package org.softwood.processEngine

import groovy.util.logging.Slf4j
import org.softwood.processLibrary.ProcessTemplateLibrary
import org.softwood.processLibrary.StandardProcessTemplateInstance
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

import java.beans.PropertyChangeListener
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
    String status = RuntimeStatus.Uninitialised
    final List runtimeList =[] as ObservableList



    ProcessRuntime () {
        status = RuntimeStatus.Running
        runtimeList << this  //add self to list
    }

    void addProcessRuntimeListener (PropertyChangeListener listener) {
        runtimeList.addPropertyChangeListener {}(listener)
    }

    void shutdown () {
        status = RuntimeStatus.Shutdown
        runtimeList.remove (this)  //fire any property change listeners
    }

    @Autowired
    ProcessTemplateLibrary processLibrary

    void startProcess (String processTemplateName) {

        ProcessInstance processInstance = new ProcessInstance ('standard')
        //todo lookup process in library
        if (processTemplateName == 'standard') {
            //todo refactor later
            processInstance = StandardProcessTemplateInstance.helloWorldProcess2()

        } else {
            //todo add search to interface
            // processLibrary.search (processTemplateName)
        }

    }
}


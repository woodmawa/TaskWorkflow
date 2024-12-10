package org.softwood.processEngine

import groovy.util.logging.Slf4j
import org.softwood.processLibrary.ProcessLibrary
import org.softwood.processLibrary.StandardProcessDefinitionTemplateImpl
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

import java.time.LocalDateTime

@Component
@Slf4j
class ProcessRuntime {
    LocalDateTime started = LocalDateTime.now()
    String status

    ProcessRuntime () {
        status = "Running"
    }

    @Autowired
    ProcessLibrary processLibrary

    void startProcess (String processTemplateName) {

        ProcessInstance processInstance = new ProcessInstance ('standard')
        //todo lookup process in library
        if (processTemplateName == 'standard') {
            //todo refactor later
            processInstance = StandardProcessDefinitionTemplateImpl.helloWorldProcess2()

        } else {
            //todo add search to interface
            // processLibrary.search (processTemplateName)
        }

    }
}

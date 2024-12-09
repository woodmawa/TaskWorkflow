package org.softwood.processEngine

import groovy.util.logging.Slf4j
import org.softwood.processLibrary.ProcessLibrary
import org.softwood.processLibrary.StandardProcessDefinitionTemplateImpl
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
@Slf4j
class ProcessRuntime {
    void startProcess (String processTemplateName) {

        //@Autowired
        ProcessLibrary processLibrary

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

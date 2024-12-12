package org.softwood.processLibrary

import org.softwood.graph.TaskGraph
import org.softwood.processEngine.ProcessInstance

interface ProcessTemplate {
    String getName ()
    String getVersion ()
    void setProcessDefinition (TaskGraph definition)
    TaskGraph getProcessDefinition ()

    ProcessInstance start (Map processVariables )

}
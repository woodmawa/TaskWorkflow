package org.softwood.processLibrary

import org.softwood.graph.TaskGraph

interface ProcessTemplate {
    String getName ()
    String getVersion ()
    void setProcessDefinition (TaskGraph definition)

}
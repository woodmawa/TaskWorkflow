package org.softwood.processLibrary

import org.softwood.graph.Edge
import org.softwood.graph.Graph
import org.softwood.graph.Vertex

class StandardProcessDefinitionTemplateImpl implements ProcessTemplate {

    String name

    StandardProcessDefinitionTemplateImpl (String templateName) {
        this.name = templateName
    }

    static Graph helloWorldProcess () {
        Graph graph = new Graph()
        graph.addVertex('start')
        graph.addVertex ('helloTask')
        graph.addVertex ('end')

        graph.addEdge ('start', 'helloTask')
        graph.addEdge('helloTask', 'end')

        return graph
    }
}


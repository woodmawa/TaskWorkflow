package org.softwood.processLibrary

import org.jgrapht.Graph
import org.jgrapht.graph.DefaultEdge
import org.jgrapht.graph.SimpleDirectedGraph

import org.softwood.graph.TaskGraph

class StandardProcessDefinitionTemplateImpl implements ProcessTemplate {

    String name
    String version
    TaskGraph processDefinition

    StandardProcessDefinitionTemplateImpl (String templateName, String version="1.0") {
        this.name = templateName
        this.version = version
    }

    String getName() {
        name
    }

    String getVersion() {
        version
    }

    void setProcessDefinition ( TaskGraph definition) {
        this.processDefinition = definition
    }

    TaskGraph getProcessDefinition ( ) {
        processDefinition
    }

    static TaskGraph helloWorldProcessDirected () {
        TaskGraph graph = new TaskGraph()
        graph.addVertex('start')
        graph.addVertex ('helloTask')
        graph.addVertex ('decision')
        graph.addVertex ('goLeft')
        graph.addVertex ('goRight')
        graph.addVertex ('join')
        graph.addVertex ('end')

        graph.addEdge ('start', 'helloTask')
        graph.addEdge('helloTask', 'decision')
        graph.addEdge('decision', 'goLeft')
        graph.addEdge('decision', 'goRight')
        graph.addEdge('goLeft', 'join')
        graph.addEdge('goRight', 'join')
        graph.addEdge('join', 'end')



        return graph
    }


    static Graph helloWorldProcess2 () {
        Graph graph = new SimpleDirectedGraph(DefaultEdge)
        graph.addVertex('start')
        graph.addVertex ('helloTask')
        graph.addVertex ('decision')
        graph.addVertex ('goLeft')
        graph.addVertex ('goRight')
        graph.addVertex ('join')
        graph.addVertex ('end')

        graph.addEdge ('start', 'helloTask')
        graph.addEdge('helloTask', 'decision')
        graph.addEdge('decision', 'goLeft')
        graph.addEdge('decision', 'goRight')
        graph.addEdge('goLeft', 'join')
        graph.addEdge('goRight', 'join')
        graph.addEdge('join', 'end')



        return graph
    }
}


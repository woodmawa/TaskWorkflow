package org.softwood.processLibrary

import org.jgrapht.Graph
import org.jgrapht.graph.DefaultEdge
import org.jgrapht.graph.SimpleDirectedGraph

import org.softwood.graph.WillsGraph

class StandardProcessDefinitionTemplateImpl implements ProcessTemplate {

    String name

    StandardProcessDefinitionTemplateImpl (String templateName) {
        this.name = templateName
    }

    static WillsGraph helloWorldProcessDirected () {
        WillsGraph graph = new WillsGraph()
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


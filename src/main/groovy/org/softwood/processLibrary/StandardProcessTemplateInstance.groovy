package org.softwood.processLibrary

import groovy.transform.ToString
import org.jgrapht.Graph
import org.jgrapht.graph.DefaultEdge
import org.jgrapht.graph.SimpleDirectedGraph

import org.softwood.graph.TaskGraph
import org.softwood.processEngine.ProcessInstance
import org.softwood.tryout.SpringContextUtils
import org.springframework.beans.factory.annotation.Lookup
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.config.ConfigurableBeanFactory
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Component

@ToString
@Component ("processTemplate")
@Qualifier ("standard")
@Scope (ConfigurableBeanFactory.SCOPE_PROTOTYPE)
class StandardProcessTemplateInstance implements ProcessTemplate {

    String name
    String version
    TaskGraph processDefinition

    //do dynamic lookup to get new prototype bean
    @Lookup ("processTemplateInstance")
    public ProcessInstance getProcessInstance () {
        null  //spring will override this and return prtototype
    }
    //ProcessInstance processInstance


    StandardProcessTemplateInstance (String templateName, String version) {
        this.name = templateName
        this.version = version
    }

    StandardProcessTemplateInstance() {
        this.name = null
        this.version = null
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

    @Override
    ProcessInstance start (Map processVariables=[:]) {

        //get prototyped scoped processInstance and set the template to be this
        ProcessInstance processInstance = SpringContextUtils.getPrototypeBean(ProcessInstance, [processVariables:processVariables])
        processInstance.processTemplate = this


        processInstance.setProcessTemplate(this)
        processInstance.start (processVariables)
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


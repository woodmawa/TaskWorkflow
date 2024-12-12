package org.softwood.processLibrary

import groovy.transform.ToString
import groovy.util.logging.Slf4j
import org.jgrapht.Graph
import org.jgrapht.graph.DefaultEdge
import org.jgrapht.graph.SimpleDirectedGraph
import org.softwood.gatewayTypes.ExclusiveGateway
import org.softwood.gatewayTypes.JoinGateway
import org.softwood.graph.TaskGraph
import org.softwood.processEngine.ProcessInstance
import org.softwood.taskTypes.EndTask
import org.softwood.taskTypes.ScriptTask
import org.softwood.taskTypes.StartTask
import org.softwood.tryout.SpringContextUtils
import org.springframework.beans.factory.annotation.Lookup
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.config.ConfigurableBeanFactory
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Component

@ToString
@Slf4j
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

    /**
     *
     * @return
     */
    @Override
    TaskGraph getProcessDefinition () {
        processDefinition
    }


    @Override
    ProcessInstance start (Map processVariables=[:]) {

        //get prototyped scoped processInstance and set the template to be this, and set processVariales on that instance
        ProcessInstance processInstance = SpringContextUtils.getPrototypeBean(ProcessInstance, [processVariables:processVariables])
        processInstance.processTemplate = this
        processInstance.setProcessTemplate(this)

        log.info  "process " + processInstance.processId + " started "
        processInstance.start (processVariables)
    }

    static TaskGraph helloWorldProcessDirected () {
        TaskGraph graph = new TaskGraph()
        def start = graph.addVertex('start', StartTask)
        def hello = graph.addVertex ('helloTask', ScriptTask)
        def decision =graph.addVertex ('decision', ExclusiveGateway)
        def goLeft = graph.addVertex ('goLeft', ScriptTask)
        def goRight = graph.addVertex ('goRight', ScriptTask)
        def join = graph.addVertex ('join', JoinGateway )
        def end = graph.addVertex ('end', EndTask)

        graph.addEdge (start, hello)
        graph.addEdge(hello, decision)
        graph.addEdge(decision, goLeft)
        graph.addEdge(decision, goRight)
        graph.addEdge(goLeft, join)
        graph.addEdge(goRight, join)
        graph.addEdge(join, end)



        return graph
    }

/*
    static Graph helloWorldProcess2 () {
        Graph graph = new SimpleDirectedGraph(DefaultEdge)

        //refactor addVertex would need to be a vertex - not a string
        def start = graph.addVertex('start', StartTask)
        def hello = graph.addVertex ('helloTask',ScriptTask)
        def decision = graph.addVertex ('decision', ExclusiveGateway)
        def goLeft = graph.addVertex ('goLeft', ScriptTask)
        def goRight = graph.addVertex ('goRight', ScriptTask)
        def join = graph.addVertex ('join', JoinGateway)
        def end = graph.addVertex ('end')

        graph.addEdge (start, hello)
        graph.addEdge(hello, decision)
        graph.addEdge(decision, goLeft)
        graph.addEdge(decision, goRight)
        graph.addEdge(goLeft, join)
        graph.addEdge(goRight, join)
        graph.addEdge(join, end)



        return graph
    }
    */

}


package org.softwood.tryout

import org.jgrapht.Graph
import org.jgrapht.graph.DefaultEdge
import org.jgrapht.traverse.DepthFirstIterator
import org.softwood.processEngine.ProcessInstance
import org.softwood.processEngine.ProcessRuntime
import org.softwood.processLibrary.ProcessTemplate
import org.softwood.processLibrary.ProcessTemplateLibrary
import org.softwood.taskTypes.ScriptTask
import org.softwood.taskTypes.Task
import org.softwood.basics.WorkflowExecutionContext
import org.softwood.basics.WorkflowExecutionContextImpl

import org.softwood.graph.TaskGraph
import org.softwood.processLibrary.StandardProcessDefinitionTemplateImpl

import java.util.concurrent.CompletableFuture

//import com.test.ApplicationConfiguration

var ctx = SpringContextUtils::initialise(activeProfile='dev', ["org.softwood.processEngine", "org.softwood.processLibrary"])

//ctx.register(ApplicationConfiguration)
//ctx.refresh()

ProcessRuntime rt = SpringContextUtils::getBean(ProcessRuntime)
println "process runtime from ctx (status: ${rt.status})> "

ProcessTemplateLibrary library = SpringContextUtils::getQualifiedBean (ProcessTemplateLibrary, "default")
println "default library is ${library.name}"


Task task = new ScriptTask()
CompletableFuture future = task.execute()
 //wait for result

println task.dump()

println task.executionDuration()
future.thenApply {result ->
    println "result from async task execution was $result"
}


WorkflowExecutionContext wf = new WorkflowExecutionContextImpl('proc#1', 'default')

wf.start([name:'william'])
wf.stop()
println "proc var " + wf.processVariables

TaskGraph g = StandardProcessDefinitionTemplateImpl.helloWorldProcessDirected()
 result = TaskGraph.depthFirstTraversal(g, 'start')

println result

var toVertices = g.getToVertices('decision')
println "decision node has options to goto $toVertices"

Graph<String, DefaultEdge> g2 = StandardProcessDefinitionTemplateImpl.helloWorldProcess2()
DepthFirstIterator gi = new DepthFirstIterator(g2)
List result2 = []
while ( gi.hasNext())  {
    result2 << gi.next().toString()
}

println result2

//create start-2-stop process
TaskGraph graph = new TaskGraph()
graph.addVertex("start")
graph.addVertex("end")
graph.addEdge("start", "end")

ProcessTemplate procDef = new StandardProcessDefinitionTemplateImpl("myProcess", version="1.0")
ProcessTemplate procDef2 = new StandardProcessDefinitionTemplateImpl("myProcess", version="2.0")
ProcessTemplate procDef3 = new StandardProcessDefinitionTemplateImpl("mySecondProcess", version="1.0")
procDef.processDefinition = graph
procDef2.processDefinition = graph
procDef3.processDefinition = graph

library.add (procDef)
library.add (procDef2)
library.add (procDef3)

List procs = library.search ("my")
Optional<ProcessTemplate> latest = library.latest ("myProcess")
ProcessTemplate template = latest.get()

ProcessInstance pi = template.start([var: 'will'])

println "proc " + pi.processId + " started "



SpringContextUtils::shutdown()



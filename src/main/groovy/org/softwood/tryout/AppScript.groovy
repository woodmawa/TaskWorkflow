package org.softwood.tryout

import org.jgrapht.Graph
import org.jgrapht.graph.DefaultEdge
import org.jgrapht.traverse.DepthFirstIterator
import org.softwood.processEngine.ProcessRuntime
import org.softwood.taskTypes.ScriptTask
import org.softwood.taskTypes.Task
import org.softwood.basics.WorkflowExecutionContext
import org.softwood.basics.WorkflowExecutionContextImpl

import org.softwood.graph.WillsGraph
import org.softwood.processLibrary.StandardProcessDefinitionTemplateImpl

import java.util.concurrent.CompletableFuture

//import com.test.ApplicationConfiguration

import org.springframework.context.annotation.AnnotationConfigApplicationContext


def activeProfile = 'test'

var ctx = SpringScriptContext::initialise(activeProfile, ["org.softwood.processEngine", "org.softwood.processLibrary"])

//ctx.register(ApplicationConfiguration)
//ctx.refresh()




ProcessRuntime rt = ctx.getBean("processRuntime")
println "process runtime from ctx > " + rt.inspect()

ctx.close()
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

WillsGraph g = StandardProcessDefinitionTemplateImpl.helloWorldProcessDirected()
 result = WillsGraph.depthFirstTraversal(g, 'start')

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

println "closing spring context "
ctx.close()



package org.softwood.tryout

import org.jgrapht.Graph
import org.jgrapht.graph.DefaultEdge
import org.jgrapht.traverse.DepthFirstIterator
import org.softwood.processEngine.ProcessInstance
import org.softwood.processEngine.ProcessRuntime
import org.softwood.processLibrary.ProcessTemplate
import org.softwood.processLibrary.ProcessTemplateLibrary
import org.softwood.taskTypes.EndTask
import org.softwood.taskTypes.ScriptTask
import org.softwood.taskTypes.StartTask
import org.softwood.taskTypes.Task
import org.softwood.basics.WorkflowExecutionContext
import org.softwood.basics.WorkflowExecutionContextImpl

import org.softwood.graph.TaskGraph
import org.softwood.processLibrary.StandardProcessTemplateInstance
import org.softwood.taskTypes.TaskTrait
import org.springframework.beans.factory.annotation.Lookup

import java.util.concurrent.CompletableFuture

//import com.test.ApplicationConfiguration

var ctx = SpringContextUtils::initialise(activeProfile='dev', ["org.softwood.processEngine", "org.softwood.processLibrary", "org.softwood.processBeanConfiguration"])

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

TaskGraph g = StandardProcessTemplateInstance.helloWorldProcessDirected()
 result = TaskGraph.depthFirstTraversal(g, g.head)

println result

var toVertices = g.getToVertices('decision')
println "decision node has options to goto $toVertices"

/*

//using library for graph
Graph<String, DefaultEdge> g2 = StandardProcessTemplateInstance.helloWorldProcess2()
DepthFirstIterator gi = new DepthFirstIterator(g2)
List result2 = []
while ( gi.hasNext())  {
    result2 << gi.next().toString()
}

println result2
*/

class X implements TaskTrait {

    @Override
    CompletableFuture execute() {
        return null
    }

    @Override
    CompletableFuture execute(Map inputVariables) {
        return null
    }

    @Override
    void setTaskType(String name) {

    }

    @Override
    String getTaskType() {
        return null
    }
}

//TaskTrait x = new X()
//x.setTaskName =

//create start-2-stop process
TaskGraph graph = new TaskGraph()
def start = graph.addVertex("start", StartTask)
def script = graph.addVertex("script", ScriptTask)
def end = graph.addVertex("end", EndTask)
graph.addEdge(start, script)
graph.addEdge(script, end)

/*@Lookup ("processTemplateInstance")
ProcessTemplate getProcessTemplate () {  //String name, version ="1.0"
    null  //will be replaced
}*/

//ProcessTemplate t1 = ctx.getBeanFactory().getBean('processTemplate')
ProcessTemplate t2 = SpringContextUtils.getPrototypeBean(ProcessTemplate, [name:"dummyProcess", version:"1.0", processDefinition:graph])
//ProcessTemplate t1 = getProcessTemplate ()

//create three prototypes - same graph in each but different names
ProcessTemplate procDef = SpringContextUtils.getPrototypeBean(ProcessTemplate, [name:"myProcess", version:"1.0", processDefinition:graph])
ProcessTemplate procDef2 =SpringContextUtils.getPrototypeBean(ProcessTemplate, [name:"myProcess", version:"2.0", processDefinition:graph])
ProcessTemplate procDef3 =SpringContextUtils.getPrototypeBean(ProcessTemplate, [name:"mySecondProcess", version:"1.0", processDefinition:graph])


library.add (procDef)
library.add (procDef2)
library.add (procDef3)

List procs = library.search ("my")
Optional<ProcessTemplate> latest = library.latest ("myProcess")
ProcessTemplate template = latest.get()

ProcessInstance pi = template.start([var: 'will'])



SpringContextUtils::shutdown()



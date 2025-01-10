package org.softwood.tryout


import org.softwood.gatewayTypes.ExclusiveGateway
import org.softwood.gatewayTypes.JoinGateway
import org.softwood.gatewayTypes.ParallelGateway
import org.softwood.processEngine.ProcessHistory
import org.softwood.processEngine.ProcessInstance
import org.softwood.processEngine.ProcessRuntime
import org.softwood.processEngine.TaskHistory
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
import org.softwood.taskTypes.TerminateTask

import java.util.concurrent.CompletableFuture


var ctx = SpringContextUtils::initialise(activeProfile='dev', ["org.softwood.processEngine", "org.softwood.processLibrary", "org.softwood.processBeanConfiguration"])

ProcessRuntime rt = SpringContextUtils::getBean(ProcessRuntime)
def status = rt.status
// now failing !  println "process runtime from ctx (status: ${status})> "

ProcessTemplateLibrary library = SpringContextUtils::getQualifiedBean (ProcessTemplateLibrary, "default")
println "default library is ${library.name}"


//Task task = new ScriptTask()
//CompletableFuture future = task.execute()
 //wait for result

//println task.dump()

/*
println task.executionDuration()
future.thenApply {result ->
    println "result from async task execution was $result"
}
*/


/*
WorkflowExecutionContext wf = new WorkflowExecutionContextImpl('proc#1', 'default')

wf.start([name:'william'])
wf.stop()
println "proc var " + wf.processVariables

TaskGraph g = StandardProcessTemplateInstance.helloWorldProcessDirected()
 result = TaskGraph.depthFirstTraversal(g, g.head)

println result

var toVertices = g.getToVertices('decision')
println "decision node has options to goto $toVertices"
*/

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


//create start-2-stop process
TaskGraph graph = new TaskGraph()
def start = graph.addVertex("start", StartTask)
def script = graph.addVertex("script", ScriptTask)
def decision = graph.addVertex("decision", ExclusiveGateway)
def par = graph.addVertex("decision", ExclusiveGateway)
def end = graph.addVertex("end", EndTask)
graph.addEdge(start, script)
graph.addEdge(script, decision)
Map cond = [check:{if (it=='Will') true else false}]
graph.addEdgeWithCondition(decision, end, cond )

TaskGraph graph2 = new TaskGraph()
def start2 = graph2.addVertex("start2", StartTask)
def start2Alt = graph2.addVertex("start2", StartTask)
def script2 = graph2.addVertex("script2", ScriptTask)
script2.scriptText = """
//default closure script 
System.exit(1)

"""

def par2 = graph2.addVertex("fork2", ParallelGateway)

def leftFork2 = graph2.addVertex("leftFork2", ScriptTask, "println '\t**> leftFork2: leftForkSelected'")
def rightFork2 = graph2.addVertex("rightFork2", ScriptTask, "println '\t**> rightFork2: rightForkSelected'")

def join2 = graph2.addVertex("join2", JoinGateway)
def terminate2 = graph2.addVertex("term2", TerminateTask)
//def end2 = graph2.addVertex("end", EndTask)

graph2.addEdge(start2, script2)
graph2.addEdge(script2, par2)
graph2.addEdge(par2, leftFork2)
graph2.addEdge(par2, rightFork2)
graph2.addEdge(leftFork2, join2)
graph2.addEdge(rightFork2, join2)
graph2.addEdge(join2, terminate2)

/*@Lookup ("processTemplateInstance")
ProcessTemplate getProcessTemplate () {  //String name, version ="1.0"
    null  //will be replaced
}*/

//ProcessTemplate t1 = ctx.getBeanFactory().getBean('processTemplate')
ProcessTemplate t2 = SpringContextUtils.getPrototypeBean(ProcessTemplate, [name:"dummyProcess", version:"1.0", processDefinition:graph])
//ProcessTemplate t1 = getProcessTemplate ()

//create three prototypes - same graph in each but different names
/*
ProcessTemplate procDef = SpringContextUtils.getPrototypeBean(ProcessTemplate, [name:"myProcess", version:"1.0", processDefinition:graph])
ProcessTemplate procDef2 =SpringContextUtils.getPrototypeBean(ProcessTemplate, [name:"myProcess", version:"2.0", processDefinition:graph])
ProcessTemplate procDef3 =SpringContextUtils.getPrototypeBean(ProcessTemplate, [name:"mySecondProcess", version:"1.0", processDefinition:graph])
*/
ProcessTemplate procDef4 =SpringContextUtils.getPrototypeBean(ProcessTemplate, [name:"gatewayProcess", version:"1.0", processDefinition:graph2])



//library.add (procDef)
//library.add (procDef2)
//library.add (procDef3)
library.add (procDef4)

//List procs = library.search ("my")
//Optional<ProcessTemplate> latest = library.latest ("myProcess")
Optional<ProcessTemplate> latestGw = library.latest ("gatewayProcess")
//ProcessTemplate template = latest.get()
ProcessTemplate template2 = latestGw.get()

//ProcessInstance pi = template.startProcess([var: 'will'])


ProcessInstance pi = template2.startProcess([var: 'will'])

println "process completed in : ${pi.executionDuration()}"

List completedProcess = ProcessHistory.completedProcesses
completedProcess.each  {
    println "completed processes : " + it
}

List completedTasks = TaskHistory.completedTasks
completedTasks.each  {
    println "completed tasks : " + it
}

SpringContextUtils::shutdown()



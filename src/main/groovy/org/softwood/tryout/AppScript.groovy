package org.softwood.tryout

import org.softwood.basics.ScriptTask
import org.softwood.basics.Task
import org.softwood.basics.WorkflowExecutionContext
import org.softwood.basics.WorkflowExecutionContextImpl
import org.softwood.graph.Graph
import org.softwood.processLibrary.ProcessTemplate
import org.softwood.processLibrary.StandardProcessDefinitionTemplateImpl

Task task = new ScriptTask()
task.execute()

println task.dump()

println task.executionDuration()


WorkflowExecutionContext wf = new WorkflowExecutionContextImpl('proc#1', 'default')

wf.start([name:'william'])
wf.stop()
println "proc var " + wf.processVariables

Graph g = StandardProcessDefinitionTemplateImpl.helloWorldProcess()
var result = Graph.depthFirstTraversal(g, 'start')

println result




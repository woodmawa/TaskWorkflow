package org.softwood.tryout

import org.softwood.basics.ScriptTask
import org.softwood.basics.Task
import org.softwood.basics.WorkflowExecutionContext
import org.softwood.basics.WorkflowExecutionContextImpl

Task task = new ScriptTask()
task.execute()

println task.dump()

println task.executionDuration()


WorkflowExecutionContext wf = new WorkflowExecutionContextImpl('proc#1', 'default')

wf.start([name:'william'])
wf.stop()
println "proc var " + wf.processVariables



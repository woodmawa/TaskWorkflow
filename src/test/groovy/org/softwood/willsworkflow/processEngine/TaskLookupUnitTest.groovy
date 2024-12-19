package org.softwood.willsworkflow.processEngine

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.softwood.graph.Vertex
import org.softwood.processEngine.ProcessInstance
import org.softwood.processEngine.ProcessRuntime
import org.softwood.processEngine.TaskTypeLookup
import org.softwood.processLibrary.DefaultProcessLibrary
import org.softwood.processLibrary.StandardProcessTemplateInstance
import org.softwood.taskTypes.StartTask
import org.softwood.taskTypes.Task
import org.softwood.taskTypes.TaskCategories
import org.softwood.taskTypes.TaskStatus
import org.softwood.willsworkflow.WillsWorkflowApplication
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Import
import org.springframework.test.context.ContextConfiguration
import org.springframework.util.Assert

//@ExtendWith(SpringExtension.class)
@SpringBootTest (classes =  [WillsWorkflowApplication.class])
@ContextConfiguration (classes = [ TaskTypeLookup, ProcessInstance, ProcessRuntime, StandardProcessTemplateInstance, DefaultProcessLibrary])

//@ComponentScan (basePackages = ["org.softwood.willsworkflow","org.softwood.processEngine", "org.softwood.processLibrary", "org.softwood.processBeanConfiguration"])
class TaskLookupUnitTest {

    @Autowired
    ApplicationContext applicationContext

    @Autowired
    private TaskTypeLookup taskTypeLookup

    @Autowired
    ProcessRuntime rt

    @Test
    void taskLookupTest () {

        Vertex start = new Vertex("start-test", StartTask)
        Optional<Task> lkp = taskTypeLookup.getTaskFor(start, [:])
        Task task = lkp.get()
        assert task.taskName == "start-test"
        assert task.taskCategory == TaskCategories.Task
        assert task.taskType == StartTask.class.getSimpleName()
        assert task.status == TaskStatus.PENDING
        assert task.startTime == null
        assert task.endTime == null
        assert task.taskVariables == [:]

    }

    @Test
    void ContextLoadAndProcessRuntimeInContextUnitTest() {
        assert applicationContext

        def res = applicationContext.getBean("processRuntime")
        assert res
    }

}

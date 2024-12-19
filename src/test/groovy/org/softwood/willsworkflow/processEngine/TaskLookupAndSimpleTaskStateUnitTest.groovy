package org.softwood.willsworkflow.processEngine

import org.junit.jupiter.api.Test
import org.softwood.graph.Vertex
import org.softwood.processEngine.ProcessInstance
import org.softwood.processEngine.ProcessRuntime
import org.softwood.processEngine.TaskTypeLookup
import org.softwood.processLibrary.DefaultProcessLibrary
import org.softwood.processLibrary.StandardProcessTemplateInstance
import org.softwood.taskTypes.ExecutableTaskTrait
import org.softwood.taskTypes.StartTask
import org.softwood.taskTypes.Task
import org.softwood.taskTypes.TaskCategories
import org.softwood.taskTypes.TaskStatus
import org.softwood.willsworkflow.WillsWorkflowApplication
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.ApplicationContext
import org.springframework.test.context.ContextConfiguration

import java.beans.PropertyChangeListener
import java.time.LocalDateTime
import java.util.concurrent.CompletableFuture

//@ExtendWith(SpringExtension.class)
@SpringBootTest (classes =  [WillsWorkflowApplication.class])
@ContextConfiguration (classes = [ TaskTypeLookup, ProcessInstance, ProcessRuntime, StandardProcessTemplateInstance, DefaultProcessLibrary])

//@ComponentScan (basePackages = ["org.softwood.willsworkflow","org.softwood.processEngine", "org.softwood.processLibrary", "org.softwood.processBeanConfiguration"])
class TaskLookupAndSimpleTaskStateUnitTest {

    @Autowired
    ApplicationContext applicationContext

    @Autowired
    private TaskTypeLookup taskTypeLookup

    @Autowired
    ProcessRuntime rt

    @Test
    void taskLookupInitialisedStateTest  () {

        Vertex start = new Vertex("start-test", StartTask)
        Optional<ExecutableTaskTrait> lkp = taskTypeLookup.getTaskFor(start, [:])
        ExecutableTaskTrait task = lkp.get()
        assert task.taskName == "start-test"
        assert task.taskCategory == TaskCategories.Task
        assert task.taskType == StartTask.class.getSimpleName()
        assert task.status == TaskStatus.PENDING
        assert task.startTime == null
        assert task.endTime == null
        assert task.taskVariables == [:]

        CompletableFuture result = task.execute([startTaskState:'dummy'])
        assert result.get() == "start task completed"
        assert task.status == TaskStatus.COMPLETED
        assert task.startTime
        assert task.endTime
        assert task.startTime < task.endTime
        assert result == task.taskResult
        task.taskVariables == [startTaskState:'dummy']

    }

    @Test
    void ContextLoadAndProcessRuntimeInContextUnitTest() {
        assert applicationContext

        ProcessRuntime rt = applicationContext.getBean("processRuntime")
        assert rt
        assert rt.status == "Running"
        assert rt.started instanceof LocalDateTime

        def event
// Listener will assign event to global event variable.
        def listener = {
            event = it
        } as PropertyChangeListener

        rt.addProcessListener {} << listener


    }

}

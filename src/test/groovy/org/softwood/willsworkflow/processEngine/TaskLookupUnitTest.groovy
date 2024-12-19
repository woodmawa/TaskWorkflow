package org.softwood.willsworkflow.processEngine

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.softwood.graph.Vertex
import org.softwood.processEngine.ProcessRuntime
import org.softwood.processEngine.TaskTypeLookup
import org.softwood.taskTypes.StartTask
import org.softwood.taskTypes.Task
import org.softwood.taskTypes.TaskCategories
import org.softwood.willsworkflow.WillsWorkflowApplication
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.ComponentScan
import org.springframework.util.Assert

//@ExtendWith(SpringExtension.class)
@SpringBootTest (classes = WillsWorkflowApplication.class)
@ComponentScan (basePackageClasses = [org.softwood.processEngine.TaskTypeLookup])
class TaskLookupUnitTest {

    @Autowired
    private TaskTypeLookup taskTypeLookup

    @Autowired ProcessRuntime rt

    @Test
    void taskLookupTest () {
        Vertex start = new Vertex("start-test", StartTask)
        Task task = taskTypeLookup.getTaskFor(start, [:])
        assert task.taskName == "start-test"
        assert task.taskCategory == TaskCategories.Task
        assert task.taskType == StartTask.class.getSimpleName()
        assert rt
    }

    @Test
    void StartTaskUnitTest() {

    }

}

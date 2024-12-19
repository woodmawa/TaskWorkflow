package org.softwood.willsworkflow

import org.junit.jupiter.api.Test
import org.softwood.processBeanConfiguration.ProcessInstanceConfiguration
import org.softwood.processBeanConfiguration.ProcessLibraryConfiguration
import org.softwood.processEngine.ProcessInstance
import org.softwood.processEngine.ProcessRuntime
import org.softwood.processLibrary.DefaultProcessLibrary
import org.softwood.processLibrary.StandardProcessTemplateInstance
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.ApplicationContext
import org.junit.jupiter.api.Test
import org.springframework.context.annotation.Import
import org.springframework.test.context.ContextConfiguration

import static org.junit.jupiter.api.Assertions.*


@SpringBootTest ()
//ProcessInstance, StandardProcessTemplateInstance
@ContextConfiguration (classes = [ ProcessInstance, ProcessRuntime, StandardProcessTemplateInstance, DefaultProcessLibrary])
//@Import ([ProcessInstanceConfiguration])
class WillsWorkflowApplicationTests {

    @Autowired
    ApplicationContext applicationContext

    @Test
    void contextLoads() {
        assert applicationContext

        List beans = applicationContext.beanDefinitionNames
        beans.each {println it}
    }

    @Test
    void StartTaskUnitTest() {
        assert applicationContext

        assert applicationContext.containsBeanDefinition("processInstance")

        def res = applicationContext.getBean("processRuntime")
        assert res
    }

}

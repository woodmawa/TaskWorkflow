package org.softwood.processBeanConfiguration

import org.softwood.processLibrary.ProcessTemplate
import org.softwood.processLibrary.StandardProcessTemplateInstance
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Scope

@Configuration
class ProcessInstanceConfiguration {
    @Bean
    @Scope ("prototype")
    public ProcessTemplate processTemplate () {
        StandardProcessTemplateInstance template = new StandardProcessTemplateInstance()
        //spring utils wrapper will add the params after default constructor builds prototype
        template
    }

}

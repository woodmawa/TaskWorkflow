package org.softwood.processBeanConfiguration

import org.softwood.processLibrary.ProcessTemplate
import org.softwood.processLibrary.StandardProcessTemplateInstance
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Scope

//configures, and generates  a bean of prototype scope in context
@Configuration
class ProcessLibraryConfiguration {
    @Bean
    @Scope ("prototype")
    public ProcessTemplate processTemplate () {
        StandardProcessTemplateInstance template = new StandardProcessTemplateInstance()
        //spring utils wrapper will add the params after default constructor builds prototype
        template
    }
}

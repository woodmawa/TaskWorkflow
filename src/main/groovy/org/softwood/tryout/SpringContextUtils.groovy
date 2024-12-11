package org.softwood.tryout

import groovy.util.logging.Slf4j
import groovyjarjarantlr4.v4.runtime.misc.NotNull
import org.springframework.beans.factory.annotation.BeanFactoryAnnotationUtils
import org.springframework.context.annotation.AnnotationConfigApplicationContext

/**
 * creates script context for a groovy script  and returns it
 */
@Slf4j
class SpringContextUtils {

    static AnnotationConfigApplicationContext applicationCtx

    static AnnotationConfigApplicationContext initialise (String activeProfile, List<String> basePackages = []) {
        /**
         * start spring context and component scan for this script
         */
        AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext()
        assert ctx
        ctx.getEnvironment().setActiveProfiles(activeProfile)
        basePackages.each {
            ctx.scan("$it")
        }

        ctx.refresh()
        log.info "started scripts spring context [env:($activeProfile)] including basePackages $basePackages"
        applicationCtx = ctx
    }

    @NotNull
    static def getQualifiedBean (Class service, String qualifier) {
        BeanFactoryAnnotationUtils.qualifiedBeanOfType(applicationCtx.getBeanFactory(), service, qualifier)
    }

    @NotNull
    static def getBean (Class service) {
        applicationCtx.getBean(service)
    }

    @NotNull
    static def getBean (String serviceName) {
        applicationCtx.getBean(serviceName)
    }

    @NotNull
    static def getPrototypeBean (Class serviceClazz, Map values) {

        def factory = applicationCtx.getBeanFactory()
        def p1 = values[0]
        def p2 = values[1]
        def  bean = factory.getBean(serviceClazz)  //triggers ProcessLibraryConfiguration - zero arg constructor
        //trying to create factory bean with params is failing so - add the values from the map after creation
        values.each {entry ->
            bean["$entry.key"] = entry.value
        }

        bean
    }


    static void shutdown() {
        log.info "closing script spring context "
        applicationCtx.close()

    }

}

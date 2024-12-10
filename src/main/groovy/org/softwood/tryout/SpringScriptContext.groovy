package org.softwood.tryout

import org.springframework.context.annotation.AnnotationConfigApplicationContext

/**
 * creates script context for a groovy script  and returns it
 */
class SpringScriptContext {

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
        println "started spring context "
        ctx
    }

}

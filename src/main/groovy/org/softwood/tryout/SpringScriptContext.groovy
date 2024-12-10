package org.softwood.tryout

import groovy.util.logging.Slf4j
import org.springframework.context.annotation.AnnotationConfigApplicationContext

/**
 * creates script context for a groovy script  and returns it
 */
@Slf4j
class SpringScriptContext {

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

    static void shutdown() {
        log.info "closing script spring context "
        applicationCtx.close()

    }

}

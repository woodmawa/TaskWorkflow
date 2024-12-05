package org.softwood.basics

import groovy.util.logging.Slf4j

import java.time.LocalDateTime

@Slf4j
class ScriptTask implements Task {
    Closure script = {println "hello William"}
    LocalDateTime startTime, stopTime
    String status

    @Override
    Object execute() {
        startTime = LocalDateTime.now()
        log.info "running script "
        var result =  script()
        stopTime =LocalDateTime.now()
        status = "completed"

    }
}

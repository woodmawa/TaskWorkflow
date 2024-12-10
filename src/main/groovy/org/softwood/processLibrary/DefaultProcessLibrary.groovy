package org.softwood.processLibrary

import groovy.util.logging.Slf4j
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Component

import java.util.concurrent.ConcurrentHashMap

@Component
@Qualifier ("default")
@Slf4j
class DefaultProcessLibrary implements ProcessLibrary {
    String name = "DefaultProcessLibrary"

    ConcurrentHashMap<String, ProcessTemplate> library = [:]

    DefaultProcessLibrary() {

    }

    Object search (String filter) {

    }

    Object match (String filter) {

    }
}

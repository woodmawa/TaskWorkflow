package org.softwood.processLibrary

import groovy.util.logging.Slf4j
import org.springframework.stereotype.Component

import java.util.concurrent.ConcurrentHashMap

@Component
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

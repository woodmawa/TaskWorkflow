package org.softwood.processLibrary

import org.springframework.stereotype.Component

import java.util.concurrent.ConcurrentHashMap

@Component
class DefaultProcessLibraryImpl implements ProcessLibrary {
    ConcurrentHashMap<String, ProcessTemplate> library = [:]

    Object search (String filter) {

    }

}

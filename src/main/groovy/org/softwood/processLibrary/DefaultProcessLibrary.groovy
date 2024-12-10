package org.softwood.processLibrary

import groovy.util.logging.Slf4j
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Component

import java.time.LocalDateTime
import java.util.concurrent.ConcurrentHashMap

@Component
@Qualifier ("default")
@Slf4j
class DefaultProcessLibrary implements ProcessTemplateLibrary {
    String name = "DefaultProcessLibrary"

    ConcurrentHashMap<String, Map<String, List<Map>>> library = [:]

    DefaultProcessLibrary() {

    }

    @Override
    boolean add (ProcessTemplate process) {
        Map metadata =[:]

        metadata = [process:process, version:process.version, date: LocalDateTime.now()]
        if (library.containsKey (process.name)) {
            List listOfVersionsMetadata = library.get (process.name)
            listOfVersionsMetadata.add (metadata)  //add new version entry to list
        }
        else
            library.putIfAbsent (process.name, [metadata])

        library.containsKey (process.name)
    }

    @Override
    /** remove whole entry */
    void remove (String processName) {
        if (library.containsKey(processName)) {
            library.remove(processName)
        }
    }

    @Override
    /** just remove process version */
    void removeProcessVersion (String processName, String version) {
        if (library.containsKey(processName)) {
            List<Map> versionedEntries = library.get(processName)
            for (entry in versionedEntries) {
                if (entry.version == version)
                    versionedEntries.remove(entry)
            }
        }
    }

    @Override
    /** returns latest version on a named template definition */
    Optional<ProcessTemplate> latest (String processName) {
        if (library.containsKey(processName)) {
            List<Map> versionedEntries = library.get(processName)
            def sorted = sortedListByVersion (versionedEntries)
            if (sorted.size() > 0)
                Optional.of (sorted[0])
            else
                Optional.empty()
        }
    }


    @Override
    List<ProcessTemplate> search (String filter, String version='latest') {
        //get any process definition contains the filter key
        def  subsetFound = library.findAll {it.key.contains(filter)}
        def listOfMetadata = subsetFound.collect{it.value}
        List listOfMapEntries = listOfMetadata.flatten()

        def procs = sortedListByVersion (listOfMapEntries)

    }

    @Override
    ProcessTemplate match (String filter) {

    }

    private List<ProcessTemplate> sortedListByVersion (List<Map> processEntries ) {
        def sorted = processEntries.sort {Map p1, Map p2 -> p1.version <=> p2.version }
        def procs = sorted.collect{it.process}.reverse()

    }
}

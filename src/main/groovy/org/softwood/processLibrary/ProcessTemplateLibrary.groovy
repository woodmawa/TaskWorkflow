package org.softwood.processLibrary

interface ProcessTemplateLibrary {
    String name

    boolean add (ProcessTemplate template)
    List<ProcessTemplate> search (String name, String version)
    ProcessTemplate match  (String regex)
    Optional<ProcessTemplate> latest (String version)
    void remove (String process)
    void removeProcessVersion (String process, String version )
}

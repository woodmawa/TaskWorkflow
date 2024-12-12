package org.softwood.graph

import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString

@EqualsAndHashCode
@ToString (excludes = "type")
class Vertex {
    String name
    Class type

    Vertex(String name, Class type) {
        this.name = name
        this.type = type
    }
}
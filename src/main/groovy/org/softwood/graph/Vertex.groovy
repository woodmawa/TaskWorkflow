package org.softwood.graph

import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString

import java.util.function.Predicate

@EqualsAndHashCode (includes = "name")
@ToString (excludes = ["type", "conditionsMap"])
class Vertex {
    String name
    Class type
    Map<String, Closure> conditionsMap = [:]

    Vertex () {}

    Vertex(String name, Class type) {
        this.name = name
        this.type = type
    }

    Vertex(String name, Class type, Map<String, Closure> conditionsMap) {
        this.name = name
        this.type = type
        this.conditionsMap = conditionsMap ?: [default:{true}]  //set natural default
    }
}
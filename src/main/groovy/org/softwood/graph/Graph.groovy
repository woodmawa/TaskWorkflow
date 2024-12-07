package org.softwood.graph

import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString

class Graph {
    private Map<Vertex, List<Vertex>> adjVertices = [:]

    void addVertex(String label) {
        adjVertices.putIfAbsent(new Vertex(label), new ArrayList<>())
    }

    void removeVertex(String label) {
        Vertex v = new Vertex(label);
        adjVertices.values().stream().forEach(e -> e.remove(v))
        adjVertices.remove(new Vertex(label))
    }

    void addEdge(String label1, String label2) {
        Vertex v1 = new Vertex(label1)
        Vertex v2 = new Vertex(label2)
        adjVertices.get(v1).add(v2)
        adjVertices.get(v2).add(v1)
    }

    void removeEdge(String label1, String label2) {
        Vertex v1 = new Vertex(label1)
        Vertex v2 = new Vertex(label2)
        List<Vertex> eV1 = adjVertices.get(v1)
        List<Vertex> eV2 = adjVertices.get(v2)
        if (eV1 != null)
            eV1.remove(v2)
        if (eV2 != null)
            eV2.remove(v1)
    }

    List<Vertex> getAdjVertices(String label) {
        return adjVertices.get(new Vertex(label))
    }

    static Set<String> depthFirstTraversal(Graph graph, String root) {
        Set<String> visited = new LinkedHashSet<String>()
        Stack<String> stack = new Stack<String>()
        stack.push(root)
        while (!stack.isEmpty()) {
            String vertex = stack.pop()
            if (!visited.contains(vertex)) {
                visited.add(vertex)
                for (Vertex v : graph.getAdjVertices(vertex)) {
                    stack.push(v.name)
                }
            }
        }
        return visited
    }

    static Set<String> breadthFirstTraversal(Graph graph, String root) {
        Set<String> visited = new LinkedHashSet<String>()
        Queue<String> queue = new LinkedList<String>()
        queue.add(root)
        visited.add(root)
        while (!queue.isEmpty()) {
            //get and remove the head of the queue
            String vertex = queue.poll()
            for (Vertex v : graph.getAdjVertices(vertex)) {
                if (!visited.contains(v.name)) {
                    visited.add(v.name)
                    queue.add(v.name)
                }
            }
        }
        return visited
    }

    @EqualsAndHashCode
    @ToString
    private class Vertex {
        String name

        Vertex (String name) {
            this.name = name
        }

    }
}


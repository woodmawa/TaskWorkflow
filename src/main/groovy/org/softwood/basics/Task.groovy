package org.softwood.basics

interface Task<T> {
    T execute ()
    T execute (Map inputVariables)
    Map getTaskVariables()
    String getTaskType()
}
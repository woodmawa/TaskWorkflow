package org.softwood.tryout.tryOutTrait



import  org.codehaus.groovy.syntax.Types

Map props =  Types.properties

List keys = props.collect {it.key}

println keys
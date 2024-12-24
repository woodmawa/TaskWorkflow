package org.softwood.tryout.tryOutTrait

import groovy.transform.InheritConstructors

@InheritConstructors
class MyScript extends Script {

    Closure script = {println "closure found name : $name"}

    @Override
    Object run() {
        def result = script.call()
        println "found binding name " + name
        return result
    }
}

Binding binding = new Binding ()
binding.setVariable("name", "will")
def myScr = new MyScript (binding)

myScr.run()
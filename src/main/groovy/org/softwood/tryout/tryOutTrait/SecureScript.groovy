package org.softwood.tryout.tryOutTrait

import org.codehaus.groovy.control.CompilerConfiguration

CompilerConfiguration config = new CompilerConfiguration()
//config.setScriptBaseClass(DelegatingScript.class.name)




def shell = new GroovyShell (this.class.classLoader, new Binding(), config)
shell.setVariable("context", "will")
println "got context as ->" +  shell.getVariable("context")
def res = shell.evaluate("""
def val = getBinding().getVariable("context")
println "hello " + val  ; true""")
res

println "\t--- safeShell --- "
SafeGroovyShell safeShell = new SafeGroovyShell()
String userScript = """
def val = getBinding().getVariable("context")
println "userScript parsed, using shell.parse with context set as -> " + val 
"""

Closure scriptAsClosure  = safeShell.parse (userScript)



scriptAsClosure()

scriptAsClosure = safeShell.parse("System.exit(0)")
scriptAsClosure()

println "exit script "
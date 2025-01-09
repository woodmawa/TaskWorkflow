package org.softwood.tryout.tryOutTrait

import org.codehaus.groovy.control.CompilerConfiguration
import org.softwood.taskTypes.ScriptEvaluator

CompilerConfiguration config = new CompilerConfiguration()
//config.setScriptBaseClass(DelegatingScript.class.name)




def shell = new GroovyShell (this.class.classLoader, new Binding(), config)
shell.setVariable("context", "will")
println "got context as ->" +  shell.getVariable("context")
def res = shell.evaluate("""
def val = getBinding().getVariable("context")
println "hello " + val  ; true""")
res

SafeGroovyShell safeShell = new SafeGroovyShell()
String userScript = """
def val = getBinding().getVariable("context")
println "hello from parsed script cheap with context -> " + val 
"""

Closure parsedScript  = safeShell.parse (userScript).&run


parsedScript()

shell.evaluate("System.exit(0)")

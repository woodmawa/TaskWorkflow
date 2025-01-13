package org.softwood.taskTypes.secureScriptBase

import groovy.util.logging.Slf4j
import org.codehaus.groovy.ast.ClassNode
import org.codehaus.groovy.ast.expr.ClassExpression
import org.codehaus.groovy.ast.expr.ConstantExpression
import org.codehaus.groovy.ast.expr.DeclarationExpression
import org.codehaus.groovy.ast.expr.Expression
import org.codehaus.groovy.ast.expr.MethodCallExpression
import org.codehaus.groovy.ast.expr.MethodPointerExpression
import org.codehaus.groovy.ast.expr.VariableExpression
import org.codehaus.groovy.control.CompilationFailedException
import org.codehaus.groovy.control.CompilerConfiguration
import org.codehaus.groovy.control.MultipleCompilationErrorsException
import org.codehaus.groovy.control.customizers.ASTTransformationCustomizer
import org.codehaus.groovy.control.customizers.SecureASTCustomizer
import org.codehaus.groovy.control.customizers.SecureASTCustomizer.ExpressionChecker
import org.codehaus.groovy.control.messages.ExceptionMessage
import org.codehaus.groovy.transform.ASTTransformation
import org.softwood.taskTypes.TaskTrait
import org.codehaus.groovy.syntax.Types


@Slf4j
class SecureScriptEvaluator {

    volatile private GroovyShell shell

    private Set<String> forbiddenMethods = [
            'System.exit',
            'Runtime.exec',
            'ProcessBuilder',
            'System.getRuntime',
            'SecurityManager',
            'ClassLoader',
            'URLClassLoader'
    ] as Set

    private Set<String> forbiddenProperties = [
            'config'

    ] as Set

    private ValidationDelegate delegate = new ValidationDelegate(forbiddenMethods, forbiddenProperties)

/**
 * The secure script evaluator uses GroovyShell, to parse script as String,  and return script as closure
 * It is intended as an example of using blacklisting to prevent
 * running methods on a class - in this case, java.lang.System.  Please note that in creating
 * any secure environment, there is no substitution for using a SecurityManager.
 *
 * Among the many different calls this class prevents are:
 *   System.exit(0)
 *   Eval.me("System.exit(0)")
 *   evaluate("System.exit(0)")
 *   (new GroovyShell()).evaluate("System.exit(0)")
 *   Class.forName("java.lang.System").exit(0)
 *   System.&exit.call(0)
 *   System.getMetaClass().invokeMethod("exit",0)
 *   def s = System; s.exit(0)
 *   Script t = this; t.evaluate("System.exit(0)")
 *
 * The restrictions required, however, also prevent the following code from working:
 *   println "test"
 *   def s = "test" ; s.count("t")
 */
    SecureScriptEvaluator () {

        List allowedTokensList = Types.collect{it.properties.collect{it.key}  }.flatten()

        CompilerConfiguration config = new CompilerConfiguration()
        //set the secure base script which does the validations
        config.setScriptBaseClass(DelegatingScript.class.name)

        // Add secure imports to prevent access to System class

        def blockSystemExitExpression = { Expression expr ->
            if (expr instanceof DeclarationExpression) {
                DeclarationExpression de = expr
                if (de.rightExpression instanceof ClassExpression) {
                    ClassExpression ce = de.rightExpression
                    if (ce.type.name == 'java.lang.System') {
                        log.info "assigning System to a variable in script - not allowed  "
                        return false
                    }
                }
            }
            if (expr instanceof MethodCallExpression) {
                MethodCallExpression mce = expr
                if (mce.objectExpression instanceof ClassExpression) {
                    ClassExpression ce = mce.objectExpression
                    ClassNode nodeType = ce.type
                    String clazz = nodeType.name
                    String method = mce.methodAsString
                    if (clazz == 'java.lang.System' && method == 'exit') {
                        log.info "detected direct call on system.exit "
                        return false
                    }

                }
                if (mce.methodAsString == 'exit') {
                    log.info "calling exit method on variable '${mce.objectExpression.toString()}'- assumed to be a rogue call ,  'method exit()' not secure for parsed script   "
                    return false
                }
            }
            return true

        } as ExpressionChecker

        SecureASTCustomizer secAST = new SecureASTCustomizer().tap {
            closuresAllowed = true
            methodDefinitionAllowed = true
            //indirectImportCheckEnabled = true

            // Explicitly define allowed classes/packages
            allowedImports = [
                    'java.lang.Math',
                    'java.lang.String',
                    'java.util.List',
                    'java.util.Map',
                    'java.util.*',
                    'java.util.concurrent.*',
                    'java.time.*',
                    'groovy.util.logging.*'
                    // Add other safe classes you want to allow
            ]


            //block these as receivers
            disallowedReceiversClasses = [
                    Object,
                    Script,
                    GroovyShell,
                    Eval,
                    //Runtime??,
                    System
            ].asImmutable()


            // Optionally disable specific statements cant fnd token
            List defaultAllowedTokens = allowedTokensList
            //allowedTokens = allowedTokensList
            /*[
            PLUS,PLUS_EQUAL,PLUS_PLUS,
            EQUAL,EQUALS, PLUS_EQUAL,
            MINUS, MINUS_EQUAL, MINUS_MINUS,
            MATCH_REGEX,
            MOD, MOD_EQUAL,


    //Types.PACKAGE,
            //Types.CLASS_DEF
            // Add other tokens you want to block - cant find defintion of these
    ]*/

            disallowedStaticImports = ['java.lang.System']


        }

        secAST.addExpressionCheckers(blockSystemExitExpression)
        config.addCompilationCustomizers(secAST)


        shell = new GroovyShell(this.class.classLoader, new Binding(), config)
    }



    Closure parse (String userScriptText, TaskTrait task ) {

        shell.setVariable("processVariables", task.parentProcess.processVariables )
        shell.setVariable("taskVariables",task.taskVariables )
        shell.setVariable("task",task )
        shell.setVariable("secure", true)

        log.debug "parsing userScriptText for forbidden actions task $task.taskName : script : [$userScriptText] "
        // Create a closure from the script text
        DelegatingScript secureScript
        try {
            secureScript = (DelegatingScript) shell.parse(userScriptText)
            secureScript.setDelegate (delegate)
            secureScript
        }
        catch (MultipleCompilationErrorsException mce) {
            log.error "parsed ScriptText made used blocked features with multiple errors: ${mce.errorCollector.errors.size()}"
            mce.errorCollector.errors.each {
                if (it instanceof ExceptionMessage && it.cause instanceof SecurityException)
                    throw it.cause
            }

        }

        // return the parsed script as a closure
        return secureScript::run
    }

}

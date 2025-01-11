package org.softwood.taskTypes.secureScriptBase

import org.codehaus.groovy.ast.ModuleNode
import org.codehaus.groovy.transform.GroovyASTTransformation
import org.codehaus.groovy.ast.ASTNode
import org.codehaus.groovy.ast.MethodNode
import org.codehaus.groovy.ast.expr.ConstantExpression
import org.codehaus.groovy.ast.expr.MethodCallExpression
import org.codehaus.groovy.control.CompilePhase
import org.codehaus.groovy.control.CompilationUnit
import org.codehaus.groovy.control.SourceUnit
import org.codehaus.groovy.transform.ASTTransformation
import org.codehaus.groovy.transform.GroovyASTTransformation

@GroovyASTTransformation(phase = CompilePhase.SEMANTIC_ANALYSIS)
class SystemExitASTTransformation implements ASTTransformation {
    @Override
    void visit(ASTNode[] nodes, SourceUnit source) {
        nodes.each { node ->
            if (node instanceof ModuleNode) {
                node.methods.each { MethodNode method ->
                    method.code.visit { expr ->
                        if (expr instanceof MethodCallExpression) {
                            MethodCallExpression call = (MethodCallExpression) expr
                            if (call.methodAsString == 'exit' && call.objectExpression.text == 'System') {
                                throw new SecurityException("Usage of System.exit is not allowed")
                            }
                        }
                    }
                }
            }
        }

    }
}
    /*visit(ASTNode[] nodes, SourceUnit source) {
        source.getAST().getMethods().each { MethodNode method ->
            method.getCode().visit { node ->
                if (node instanceof MethodCallExpression) {
                    MethodCallExpression call = (MethodCallExpression) node
                    if (call.getMethodAsString() == 'exit' && call.getObjectExpression().getType().getName() == 'java.lang.System') {
                        throw new SecurityException("Usage of System.exit in safe script is not allowed")
                    }
                }
            }
        }
    }
}*/

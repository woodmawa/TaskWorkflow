package org.softwood.taskTypes.secureScriptBase

import groovy.util.logging.Slf4j

/**
 *
 * Security Features:
 *
 * - Blocks dangerous system calls like System.exit()
 * - Prevents SQL injection attempts
 * - Blocks XSS (Cross-Site Scripting) attempts
 * - Validates method calls and property access
 * - Provides input sanitization utilities
 *
 * The class will automatically:
 *
 * -Intercept and validate all method calls
 * -Check string inputs for SQL injection patterns
 * -Block XSS attempts
 * -Prevent access to dangerous system operations
 *
 * If you try to do something potentially dangerous, it will throw a SecurityException.
 *
 * abstract as it doesnt implement the run ()
 */
@Slf4j
abstract class BrokenSecureBaseScript extends Script implements GroovyInterceptable {

    //run method in extending script ...

    def delegate

    BrokenSecureBaseScript(Closure delegate=null) {
        super()
        this.delegate = delegate
    }

    private static final List<String> FORBIDDEN_METHODS = [
            'System.exit',
            'Runtime.exec',
            'ProcessBuilder',
            'System.getRuntime',
            'SecurityManager',
            'ClassLoader',
            'URLClassLoader'
    ]

    // SQL injection patterns to detect
    private static final List<String> SQL_INJECTION_PATTERNS = [
            /.*(?i)(SELECT|INSERT|UPDATE|DELETE|DROP|UNION|ALTER).*['"];.*/, // Basic SQL injection
            /.*(?i)(--).*/, // SQL comment
            /.*(?i)(OR|AND)\s+['"]?\s*\d+\s*=\s*\d+\s*['"]?/ // Logic-based injection
    ]

    // XSS patterns to detect
    private static final List<String> XSS_PATTERNS = [
            /<script\b[^>]*>.*?<\/script>/,
            /javascript:/,
            /on\w+\s*=/,
            /<img[^>]+src[^>]+>/
    ]


    def invokeMethod(String name, args) {
        log.info "method $name with ($args) invoked in script "
        MetaMethod check = metaClass.getMetaMethod("checkMethod", name, args)
        check.invoke (this, name, args)
    }

    // Override methodMissing to catch and validate method calls
    def checkMethod(String name, args) {
        def metaMethod = BrokenSecureBaseScript.metaClass.getMetaMethod("validateMethodCall", name, args)


        metaMethod.invoke (this, name, args)
        // If validation passes, proceed with the method call
        def relayMetaMethod = this.metaClass.getMetaMethod(name, args)
        if (relayMetaMethod) {
            return relayMetaMethod.invoke(this, args)
        }
        log.info "method in script [$name] is not a valid meta method that can be called "
        throw new MissingMethodException(name, this.class, args)
    }

    // Validate method calls against security rules
    private void validateMethodCall(String name, args) {
        log.info "secureBase : validate method call : validate method  : $name with args $args"

        // Check for forbidden method calls
        if (FORBIDDEN_METHODS.any { forbidden ->
            Boolean notAllowed = name.contains(forbidden) || (args?.toString()?.contains(forbidden))
            if (notAllowed) {
                log.info "not allowed to call [$forbidden] in script "
            }
        }) {
            log.info "secureBase : validate string input : $name with args $args, failed as contained FORBIDDEN_methods"
            throw new SecurityException("Secure Script: Forbidden method call detected: $name")
        }

        // Check all string arguments for SQL injection
        args?.each { arg ->
            if (arg instanceof String) {
                validateStringInput(arg)
            }
        }
    }

    // Validate string input against injection patterns
    private void validateStringInput(String input) {
        log.info "secureBase : validate string input : $input"
        // Check for SQL injection attempts
        if (SQL_INJECTION_PATTERNS.any { pattern ->
            input =~ pattern
        }) {
            log.info "secureBase : validate string input : [$input] , failed as contained FORBIDDEN_methods"

            throw new SecurityException("Potential SQL injection detected in input: ${input.take(50)}...")
        }

        // Check for XSS attempts
        if (XSS_PATTERNS.any { pattern ->
            input =~ pattern
        }) {
            log.info "secureBase : validate string input : [$input], failed as contained XSS patterns"

            throw new SecurityException("Potential XSS attack detected in input: ${input.take(50)}...")
        }
    }

    // Override property access
    def propertyMissing(String name) {
        validatePropertyAccess(name)
        log.info "property $name wasnt found, throwing exception"
        throw new MissingPropertyException(name, this.class)
    }

    // Validate property access against security rules
    private void validatePropertyAccess(String name) {
        if (FORBIDDEN_METHODS.any { forbidden -> name.contains(forbidden) }) {
            log.info "secureBase : validate property access  : $name, failed as contains forbidden methods "
            throw new SecurityException("Access to forbidden property detected: $name")
        }
    }

    // Sanitize string input (utility method for subclasses)
    protected String sanitizeInput(String input) {
        if (!input) return input

        // Basic HTML encoding
        return input
                .replaceAll(/&/, '&amp;')
                .replaceAll(/</, '&lt;')
                .replaceAll(/>/, '&gt;')
                .replaceAll(/"/, '&quot;')
                .replaceAll(/'/, '&#x27;')
                .replaceAll(/\(/, '&#40;')
                .replaceAll(/\)/, '&#41;')
    }
}
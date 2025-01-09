package org.softwood.taskTypes.secureScriptBase

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
abstract class SecureBaseScript extends Script {

    //run method in extending script ...

    def delegate

    SecureBaseScript(Closure delegate=null) {
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

    // Override methodMissing to catch and validate method calls
    def methodMissing(String name, args) {
        validateMethodCall(name, args)
        // If validation passes, proceed with the method call
        def metaMethod = this.metaClass.getMetaMethod(name, args)
        if (metaMethod) {
            return metaMethod.invoke(this, args)
        }
        throw new MissingMethodException(name, this.class, args)
    }

    // Validate method calls against security rules
    private void validateMethodCall(String name, args) {
        // Check for forbidden method calls
        if (FORBIDDEN_METHODS.any { forbidden ->
            name.contains(forbidden) || (args?.toString()?.contains(forbidden))
        }) {
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
        // Check for SQL injection attempts
        if (SQL_INJECTION_PATTERNS.any { pattern ->
            input =~ pattern
        }) {
            throw new SecurityException("Potential SQL injection detected in input: ${input.take(50)}...")
        }

        // Check for XSS attempts
        if (XSS_PATTERNS.any { pattern ->
            input =~ pattern
        }) {
            throw new SecurityException("Potential XSS attack detected in input: ${input.take(50)}...")
        }
    }

    // Override property access
    def propertyMissing(String name) {
        validatePropertyAccess(name)
        throw new MissingPropertyException(name, this.class)
    }

    // Validate property access against security rules
    private void validatePropertyAccess(String name) {
        if (FORBIDDEN_METHODS.any { forbidden -> name.contains(forbidden) }) {
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
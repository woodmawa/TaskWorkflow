package org.softwood.taskTypes.secureScriptBase

import groovy.util.logging.Slf4j

@Slf4j
class ValidationDelegate {
    private Set<String> forbiddenMethods
    private Set<String> forbiddenProperties
    private Map<String, Object> propertyValues = [:]
    private List<String> methodCallHistory = []
    private List<String> propertyAccessHistory = []

    private static final Set<String> INTERNAL_PROPERTIES = ['out'] as Set  //out is set by groovy implictly for println

    ValidationDelegate(Set<String> forbiddenMethods, Set<String> forbiddenProperties) {
        this.forbiddenMethods = forbiddenMethods
        this.forbiddenProperties = forbiddenProperties
    }

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


    def methodMissing(String name, args) {
        methodCallHistory << name

        if (forbiddenMethods.contains(name)) {
            log.info "method $name in banned list"
            throw new SecurityException("Method '$name' is in the forbidden methods list")
        }

        // Check all string arguments for SQL injection
        args?.each { arg ->
            if (arg instanceof String) {
                validateStringInput(arg)
            }
        }
        log.info  "Called missing method: $name in script with args: $args"
    }

    def propertyMissing(String name) {
        propertyAccessHistory << "get:$name"

        if (INTERNAL_PROPERTIES.contains(name)) {
            return System.out  // or your preferred output handler
        }

        if (forbiddenProperties.contains(name)) {
            log.info "property $name in banned list"
            throw new SecurityException("Property '$name' is in the forbidden  properties list")
        }

        log.info  "propertyMissing: Called for property : $name "
        return propertyValues.get(name)
    }

    def propertyMissing(String name, value) {
        propertyAccessHistory << "set:$name"

        if (INTERNAL_PROPERTIES.contains(name)) {
            return System.out  // or your preferred output handler
        }

        if (forbiddenProperties.contains(name)) {
            log.info "cant set property $name as its in the banned list"

            throw new SecurityException("Property '$name' is in the forbidden properties list")
        }

        propertyValues[name] = value
    }

    // Getters for validation history
    List<String> getMethodCallHistory() {
        return methodCallHistory.asImmutable()
    }

    List<String> getPropertyAccessHistory() {
        return propertyAccessHistory.asImmutable()
    }

    Map<String, Object> getPropertyValues() {
        return propertyValues.asImmutable()
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


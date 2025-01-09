package org.softwood.tryout.tryOutTrait.secure

abstract class BaseScript extends Script {
    // Add security checks
    def methodMissing(String name, args) {
        throw new SecurityException("Method $name is not allowed")
    }

    // Add property access control
    def propertyMissing(String name) {
        throw new SecurityException("Property $name is not allowed")
    }

    // Override run to ensure closure return
    abstract Object run()
}
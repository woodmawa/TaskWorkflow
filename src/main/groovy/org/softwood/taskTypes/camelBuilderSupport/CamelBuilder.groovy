package org.softwood.taskTypes.camelBuilderSupport

import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import org.apache.camel.CamelContext
import org.apache.camel.builder.RouteBuilder
import org.apache.camel.component.log.LogComponent
import org.apache.camel.model.ChoiceDefinition
import org.apache.camel.model.OnExceptionDefinition
import org.apache.camel.model.ProcessorDefinition
import org.apache.camel.model.RouteDefinition
import org.apache.camel.impl.DefaultCamelContext
import org.apache.camel.ProducerTemplate
import org.apache.camel.support.DefaultComponent
import groovy.util.FactoryBuilderSupport
import org.apache.camel.spi.Registry
import org.apache.camel.support.DefaultRegistry
import org.apache.camel.Predicate

import java.util.function.Predicate

@CompileStatic
@Slf4j
class CamelBuilder extends FactoryBuilderSupport {
    private CamelContext context
    private List<RouteDelegate> routes = []
    private Map<String, DefaultComponent> components = [:]
    private ProducerTemplate producerTemplate

    CamelBuilder() {
        Registry registry = new DefaultRegistry()
        context = new DefaultCamelContext(registry)
        producerTemplate = context.createProducerTemplate()
    }

    /**
     * method call in builder - processes components closure and adds all the
     * components from the array into the and registers them all into the camel context
     * @param components closure to process - delegatesTo ComponentBuilder
     * @return void
     */
    void components(@DelegatesTo(ComponentBuilder) Closure closure) {
        def componentBuilder = new ComponentBuilder()
        closure.delegate = componentBuilder
        closure.resolveStrategy = Closure.DELEGATE_FIRST
        closure.call()

        componentBuilder.components.each { name, component ->
            registerComponent(name, component)
        }
    }

    private def registerComponent(String name, DefaultComponent component) {
        components[name] = component
        context.addComponent(name, component)
    }

    class ComponentBuilder {
        private Map<String, DefaultComponent> components = [:]

        def logger (DefaultComponent logger) {
            components['log'] = logger
        }

        def clazz (DefaultComponent clazz) {
            components['class'] = clazz
        }

        def methodMissing(String name,  argsList ) {
            Object[] args = argsList as Object[]
            if (args.size() > 0 && args[0] instanceof DefaultComponent) {
                components[name] = args[0] as DefaultComponent
            } else if (args.size() > 0 && args[0] instanceof Closure) {
                Closure closArg = args[0] as Closure
                def component = closArg.call()
                if (component instanceof DefaultComponent) {
                    components[name] = component
                } else {
                    throw new IllegalArgumentException("Closure must return a DefaultComponent")
                }
            } else {
                throw new IllegalArgumentException("Component must be a DefaultComponent or a Closure returning a DefaultComponent")
            }
        }

        Map<String, DefaultComponent> getComponents() {
            return components
        }
    }

    /**
     * CamelBuilder #route -invoked when route definition is found in the script
     * @param closure - definition of the route as closure, delegateTo RouteBuilder
     * @return array of CamelBuilder.compiled routes
     */
    void route(@DelegatesTo(RouteDelegate) Closure closure) {
        /*def routeBuilder = new RouteBuilder() {
            @Override
            void configure() {
                closure.delegate = this
                closure.resolveStrategy = Closure.DELEGATE_FIRST
                closure.call()
            }
        }*/
        def routeDelegate = new RouteDelegate() {
            @Override
            void configure() {
                closure.delegate = this
                closure.resolveStrategy = Closure.DELEGATE_FIRST
                closure.call()
            }
        }
        routes << routeDelegate
    }

    def from(String uri) {
        def currentRouteBuilder = routes?.last()
        def routeDef = currentRouteBuilder?.from(uri)
        return new RouteDelegate(routeDef)
    }

    /**
     * Route delegate helper class to maintain proper chaining, wraps the camel RouteBuilder as a delegate
     * but provides its own overriding methods for the closure delegate in the script
     *
     */
    class RouteDelegate extends RouteBuilder {
        @Delegate private RouteBuilder currentRouteBuilder  //otherwise delegate calls to camel RouteBuilder
        private ProcessorDefinition<?> currentDefinition
        private ChoiceDefinition choiceDefinition
        private OnExceptionDefinition exceptionDefinition

        //legacy not taking RouteBuilder as closure delegate any longer - but wrapped inside a RouteDelegate
        RouteDelegate(RouteDefinition routeDefinition) {
            this.currentDefinition = routeDefinition
        }

        //new entry point sups up nee routeDelegate with wrapped route builder
        RouteDelegate() {
            super()
            this.currentRouteBuilder = this
            currentDefinition = this.routes.route()
        }

        def to(String uri) {
            if (choiceDefinition) {
                choiceDefinition.to(uri)
                return this
            }
            if (exceptionDefinition) {
                exceptionDefinition.to(uri)
                return this
            }
            currentDefinition.to(uri)
            return this
        }

        /**
         * use when you want to read headers and transform the body data
         *
         * @param transform - closure that processes the message before it goes to next step
         * @return routeDelegate
         */
        def transform ( Closure transform) {
            ProcessorDefinition target = currentDefinition
            target.process {exchange ->
                transform.delegate = this
                transform.call (exchange)
            }
            return this
        }

        /**
         * use when your want to filter out data from the message and just process the matched result output
         * @param filter
         * @return routeDelegate
         */
        def filter (Closure filter) {
            ProcessorDefinition target = currentDefinition
            target.process {exchange ->
                filter.call (exchange)
            }
            return this
        }

        def process(Closure processor) {
            def target = choiceDefinition ?: exceptionDefinition ?: currentDefinition
            target.process { exchange ->
                processor.call(exchange)
            }
            return this
        }

        def choice() {
            choiceDefinition = currentDefinition.choice()
            return this
        }

        def when(Closure predicate) {
            if (!choiceDefinition) {
                throw new IllegalStateException("when() can only be called after choice()")
            }
            choiceDefinition = choiceDefinition.when(new org.apache.camel.Predicate() {
                boolean matches(org.apache.camel.Exchange exchange) {
                    return predicate.call(exchange)
                }
            })
            return this
        }

        def otherwise() {
            if (!choiceDefinition) {
                throw new IllegalStateException("otherwise() can only be called after choice()")
            }
            choiceDefinition = choiceDefinition.otherwise()
            return this
        }

        def endChoice() {
            if (!choiceDefinition) {
                throw new IllegalStateException("endChoice() can only be called after choice()")
            }
            currentDefinition = choiceDefinition.end()
            choiceDefinition = null
            return this
        }

        /*def onException(Class<? extends Exception> exceptionClass) {
            def routeDef = currentDefinition as RouteDefinition
            exceptionDefinition = routeDef.onException(exceptionClass)
            return this
        }*/

        def handled(boolean handled) {
            if (!exceptionDefinition) {
                throw new IllegalStateException("handled() can only be called within onException()")
            }
            exceptionDefinition.handled(handled)
            return this
        }

        def endOnException() {
            if (!exceptionDefinition) {
                throw new IllegalStateException("endOnException() can only be called after onException()")
            }
            currentDefinition = exceptionDefinition.end()
            exceptionDefinition = null
            return this
        }

        def circuitBreaker(@DelegatesTo(RouteBuilder) Closure closure) {
            def target = choiceDefinition ?: currentDefinition
            def breaker = target.circuitBreaker()
            breaker.process { exchange ->
                closure.call(exchange)
            }
            if (choiceDefinition) {
                choiceDefinition = breaker.endChoice()
            } else {
                currentDefinition = breaker.end()
            }
            return this
        }

        def methodMissing(String name, args) {
            def definition = choiceDefinition ?: exceptionDefinition ?: currentDefinition
            def method = definition.metaClass.getMetaMethod(name, args)
            if (method) {
                def result = method.invoke(definition, args)
                // If the result is a ProcessorDefinition, update our current definition
                if (result instanceof ProcessorDefinition && !choiceDefinition && !exceptionDefinition) {
                    currentDefinition = result
                }
                return this
            }
            throw new MissingMethodException(name, this.class, args)
        }
    }

    /**
     * takes all the processed routes declared and adds them to the context
     * and starts the context
     * @return camelContex[running]
     */
    def build() {
        CamelContext ctx = context
        routes.each { route ->
            ctx.addRoutes(route.currentRouteBuilder )
        }
        context.start()
        return context
    }

    /**
     * stop the camel context and clean up resources
     * @return
     */
    def stop() {
        try {
            producerTemplate?.close()
            context?.stop()
        } catch (Exception e) {
            throw new RuntimeException("Error stopping Camel context", e)
        }
    }

    def triggerRoute(String routeId, Object body = null, Map headers = [:], Map properties = [:]) {
        if (!context.isStarted()) {
            throw new IllegalStateException("CamelContext is not started")
        }

        if (body != null) {
            if (!headers.isEmpty() && !properties.isEmpty()) {
                producerTemplate.send("direct:$routeId", { exchange ->
                    exchange.in.body = body
                    exchange.in.headers.putAll(headers)
                    properties.each { key, value ->
                        exchange.setProperty(key as String, value as Object)
                    }
                })
            } else if (!headers.isEmpty()) {
                producerTemplate.sendBodyAndHeaders("direct:$routeId", body, headers)
            } else {
                producerTemplate.sendBody("direct:$routeId", body)
            }
        } else {
            producerTemplate.sendBody("direct:$routeId", "")
        }
    }
}


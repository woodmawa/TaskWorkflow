package org.softwood.taskTypes.camelBuilderSupport

import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import org.apache.camel.CamelContext
import org.apache.camel.RoutesBuilder
import org.apache.camel.builder.RouteBuilder
import org.apache.camel.component.log.LogComponent
import org.apache.camel.model.ChoiceDefinition
import org.apache.camel.model.OnExceptionDefinition
import org.apache.camel.model.ProcessorDefinition
import org.apache.camel.model.RouteDefinition
import org.apache.camel.model.RoutesDefinition
import org.apache.camel.impl.DefaultCamelContext
import org.apache.camel.ProducerTemplate
import org.apache.camel.support.DefaultComponent
import groovy.util.FactoryBuilderSupport
import org.apache.camel.spi.Registry
import org.apache.camel.support.DefaultRegistry
import org.apache.camel.Predicate

import java.util.function.Predicate

//@CompileDynamic
@Slf4j
class CamelBuilder extends FactoryBuilderSupport {
    private CamelContext context
    private List<RouteDelegate> routes = []  //<? extends  RouteBuilder>
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
    void route(@DelegatesTo(RouteDelegate) Closure userClosure) {

        RouteDelegate routeDelegate = new RouteDelegate (userClosure)
        routes << routeDelegate
    }

    /**
     * create a routeDelegate that we can expand that relays to RouteBuilder as
     * delegate.  Takes the closure with the route definition from CamelBuilder closure
     * and sets the route definition closure in user script  with the RouteDelegate as its
     * closure delegate
     */
    //@CompileDynamic
    class RouteDelegate {

        private RouteDefinitionDelegate currentDefinition

        RouteBuilder routeBuilder


        RouteDelegate (Closure closure ) {
            //sets up the delegating routeBuilder
             routeBuilder = new RouteBuilder() {
                @Override
                void configure() {
                    //empty configure
                }
            }

            //takes copy of the users route script closure to set up the delegate routeBuilder
            //userRouteClosure = closure
            closure.delegate = this
            closure.resolveStrategy = Closure.DELEGATE_FIRST

            // Now call the closure
            closure.call()

        }


        RouteDefinitionDelegate from (String uri) {
            //call the delegate so we can save the routeDefinition from the builder
            def routeDefinition   = routeBuilder.from (uri)

            currentDefinition = new RouteDefinitionDelegate (routeDefinition)
            log.info "returning route definition delegate for (" + routeDefinition + ")"
            return currentDefinition as RouteDefinitionDelegate
        }




        /* if user calls and method not in routeDelegate, presume its targetted to the
         * currentDefinition and try and invoke that, else generate exception
         */
        def methodMissing (String name, args) {
            log.info "--> routeDelegate: method missing(): $name and args $args "

            if (currentDefinition == null) {
                throw new MissingMethodException(name, this.class, args)
            }

            def meth = currentDefinition.respondsTo (name, args)
            def method = currentDefinition.metaClass.getMetaMethod(name, (String) args)
            if (method) {
                //delegate call out to the return from the from()
                log.info "--> routeDelegate: found method  $name on currentDefinitionDelegate, invoking it  "

                def result = method.invoke (currentDefinition, args)
                if (result instanceof ProcessorDefinition) {
                    currentDefinition = new RouteDefinitionDelegate( result)
                }
                return this
            }
            throw new MissingMethodException (name, this.class, args)
        }

        /**
         * delegate class for route definintion, user can add own transformation methods
         * on this class, and it not defined will delegate to the actual route definition
         */
        //@CompileDynamic
        class RouteDefinitionDelegate {
            @Delegate RouteDefinition routeDefinition
            private ProcessorDefinition processDefinition
            private ChoiceDefinition  choiceDefinition
            private OnExceptionDefinition exceptionDefinition

            RouteDefinitionDelegate (RouteDefinition routeDefinition) {
                this.routeDefinition = routeDefinition
                def result = switch (routeDefinition) {
                    case {it instanceof ProcessorDefinition} -> processDefinition = routeDefinition
                    case {it instanceof ChoiceDefinition} -> choiceDefinition = routeDefinition
                    case {it instanceof OnExceptionDefinition}  -> exceptionDefinition = routeDefinition
                    default -> "no op"
                }
                log.info " setup RouteDefinitionDelegate for ($routeDefinition)"
            }

            /**
             * use when you want to read headers and transform the body data
             *
             * @param transform - closure that processes the message before it goes to next step
             * @return routeDelegate
             */
            def transform ( Closure transform) {
                log.info "transform processor ():  called on routeDefinition delegate with closure: " + transform.toString()

                        ProcessorDefinition target = currentDefinition.routeDefinition
                def result = target.process {exchange ->
                    transform.delegate = this
                    transform.call (exchange)

                }
                currentDefinition = new RouteDefinitionDelegate (result)
                return currentDefinition

            }

            /**
             * use when your want to filter out data from the message and just process the matched result output
             * @param filter
             * @return routeDelegate
             */
            def filter (Closure filter) {
                ProcessorDefinition target = currentDefinition
                //could detect # params and split into header and body ...
                def result = target.process {exchange ->
                    filter.call (exchange)
                }
                currentDefinition = new RouteDefinitionDelegate (result)
                return currentDefinition
            }

            def choice (Closure choice) {
                ChoiceDefinition target = choiceDefinition
                //could detect # params and split into header and body ...
                def result = target.choice ()
                currentDefinition = new RouteDefinitionDelegate (result)
                log.info "found choice $result, return as RouteDefinitionDelegate "
                return currentDefinition
            }
        }
    }

    /**
     * takes all the processed routes declared and adds them to the context
     * and starts the context
     * @return camelContex[running]
     */
    def build() {
        CamelContext ctx = context
        routes.each {  RouteDelegate routeDelegate ->
            ctx.addRoutes(routeDelegate.routeBuilder) //as RouteBuilder : route.currentRouteBuilder
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


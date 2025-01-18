package org.softwood.taskTypes

import groovy.util.logging.Slf4j
import org.apache.camel.Processor
import org.apache.camel.builder.RouteBuilder
import org.apache.camel.impl.DefaultCamelContext
import org.apache.camel.impl.DefaultCamelContext
import org.apache.camel.Exchange
import org.apache.camel.model.OnFallbackDefinition
import org.apache.camel.model.RouteDefinition
import org.apache.camel.model.CircuitBreakerDefinition
import org.apache.camel.model.Resilience4jConfigurationDefinition
import org.apache.camel.spi.RouteController
import org.apache.camel.support.RoutePolicySupport
import org.apache.camel.api.management.ManagedCamelContext
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ConcurrentHashMap
import groovy.transform.CompileStatic
import groovy.transform.builder.Builder
import org.apache.camel.builder.TemplatedRouteBuilder


@CompileStatic
@Builder
class CircuitBreakerConfig {
    int failureThreshold = 5
    int resetTimeout = 30000
    int halfOpenAttempts = 3
    long requestTimeout = 1000
    Closure fallback
}

@CompileStatic
@Builder
class RouteConfig {
    String routeId
    String fromEndpoint
    String resultEndpoint
    boolean enableMetrics = true
    int retryAttempts = 3
    long retryDelay = 1000
    CircuitBreakerConfig circuitBreaker
    Closure errorHandler
    Closure onComplete
    Map<String, Object> headers = [:]
}

@Slf4j
@CompileStatic
class DynamicRouteManager {

    private DefaultCamelContext camelContext
    private Map<String, RouteDefinition> activeRoutes = [:]

    DynamicRouteManager(DefaultCamelContext context) {
        this.camelContext = context
    }

    void addRoute(RouteConfig config, Closure routeDefinition) {

        if (config.circuitBreaker) {
            assert config.circuitBreaker.halfOpenAttempts > 0, "halfOpenAttempts must be greater than 0"
            assert config.circuitBreaker.failureThreshold > 0 && config.circuitBreaker.failureThreshold <= 100, "failureThreshold must be between 0 and 100"
            assert config.circuitBreaker.resetTimeout > 0, "resetTimeout must be greater than 0"
        }

        camelContext.addRoutes(new RouteBuilder() {
            @Override
            void configure() {
                // Error handler configuration
                if (config.errorHandler) {
                    errorHandler(deadLetterChannel("direct:${config.routeId}-error")
                            .maximumRedeliveries(config.retryAttempts)
                            .redeliveryDelay(config.retryDelay)
                            .logExhaustedMessageHistory(true)
                            .asyncDelayedRedelivery())

                    from("direct:${config.routeId}-error")
                            .process(new Processor() {
                                @Override
                                void process(Exchange exchange) {
                                    config.errorHandler.call(exchange)
                                }
                            })
                }

                // Create the route
                def routeDef = from(config.fromEndpoint).with {
                    routeId(config.routeId)
                    choice().with {
                        when({ config.circuitBreaker != null }).with {
                            circuitBreaker().with {
                                resilience4jConfiguration().with {
                                    failureRateThreshold(config.circuitBreaker.failureThreshold)
                                    waitDurationInOpenState(config.circuitBreaker.resetTimeout)
                                    permittedNumberOfCallsInHalfOpenState(config.circuitBreaker.halfOpenAttempts)
                                }
                                process { exchange ->
                                    // Apply the route definition here
                                    routeDefinition.delegate = this
                                    routeDefinition.resolveStrategy = Closure.DELEGATE_FIRST
                                    routeDefinition.call(exchange)
                                }
                                onFallback().with {
                                    transform().constant("Fallback response")
                                }//end on fallback

                            } //end cct breaker
                        } //end when
                        otherwise().with {//run without cct breaker
                            process { exchange ->
                                routeDefinition.delegate = this
                                routeDefinition.resolveStrategy = Closure.DELEGATE_FIRST
                                routeDefinition.call(exchange)
                            }
                        }//end otherwise
                    } //end of choice
                    to(config.resultEndpoint)
                    process { Exchange exchange ->
                        if (config.onComplete) config.onComplete.call(exchange)
                    }
                } //end of route definition 'from'
                activeRoutes.put(config.routeId, routeDef )
            } //end of configure method
        })//end new route builder

     } //end add route method


    void removeRoute(String routeId) {
        try {
            camelContext.stopRoute(routeId)
            camelContext.removeRoute(routeId)
            activeRoutes.remove(routeId)
        } catch (Exception e) {
            log.error("Error removing route: ${routeId}", e)
        }
    }
}



// Rest of the example usage remains the same

@CompileStatic
class CircuitBreakerMetrics {
    int totalCalls = 0
    int successfulCalls = 0
    int failedCalls = 0
    String state = 'CLOSED'
    long lastStateTransition = System.currentTimeMillis()
}

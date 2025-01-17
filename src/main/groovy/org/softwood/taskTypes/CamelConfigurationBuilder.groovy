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
        camelContext.addRoutes(new RouteBuilder() {
            @Override
            void configure() {
                // Configure error handling if specified
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

                // Start route definition
                RouteDefinition route = from(config.fromEndpoint)
                        .routeId(config.routeId)

                // Add circuit breaker if configured
                if (config.circuitBreaker) {
                    Processor processor = { Exchange exchange ->
                        if (config.circuitBreaker.fallback) {
                            config.circuitBreaker.fallback.call(exchange)
                        }
                    } as Processor
                    OnFallbackDefinition fallback = new OnFallbackDefinition()
                    route.circuitBreaker()
                            .resilience4jConfiguration()
                            .failureRateThreshold(config.circuitBreaker.failureThreshold)
                            .waitDurationInOpenState(config.circuitBreaker.resetTimeout)
                            .permittedNumberOfCallsInHalfOpenState(config.circuitBreaker.halfOpenAttempts)
                            .end()
                            .onFallbackViaNetwork()
                            //.onFallbackViaNetwork()
                                //.transfprocessor)
                }

                // Apply route definition
                routeDefinition.delegate = this
                routeDefinition.call()

                // Add completion handling
                route.to(config.resultEndpoint)
                        .process(new Processor() {
                            @Override
                            void process(Exchange exchange) {
                                if (config.onComplete) {
                                    config.onComplete.call(exchange)
                                }
                            }
                        })

                activeRoutes[config.routeId] = route
            }
        })
    }

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

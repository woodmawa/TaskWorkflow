package org.softwood.taskTypes

import groovy.transform.ToString
import groovy.util.logging.Slf4j
import org.apache.camel.CamelContext
import org.apache.camel.builder.RouteBuilder
import org.apache.camel.impl.DefaultCamelContext
import org.apache.camel.Exchange
import org.apache.camel.model.RouteDefinition
import org.apache.camel.model.CircuitBreakerDefinition
import org.apache.camel.model.Resilience4jConfigurationDefinition
import org.apache.camel.spi.RouteController
import org.apache.camel.support.RoutePolicySupport
import org.apache.camel.api.management.ManagedCamelContext
import org.springframework.beans.factory.annotation.Autowired

import java.util.concurrent.CompletableFuture
import java.util.concurrent.ConcurrentHashMap
import groovy.transform.CompileStatic
import groovy.transform.builder.Builder

import java.util.concurrent.CompletableFuture

@Slf4j
@ToString (includeNames=true, includes = ["taskName", "taskType", "taskCategory", "status", "startTime", "endTime"])
class CamelFlowTask implements ExecutableTaskTrait {
    String taskType = this.class.getSimpleName()
    TaskCategories taskCategory = TaskCategories.Task
    private CamelContext camelContext
    private Map<String, Closure> routeConfigurations = [:]
    private Map<String, CircuitBreakerDefinition> circuitBreakerMetrics = [:]
    private Map<String, CompletableFuture<Object>> resultFutures = [:]
    private DynamicRouteManager dynamicRouteManager

    CamelFlowTask () {
        taskWork = CamelFlowTask::startCamelFlow //link work to correct do work method

        this.camelContext = new DefaultCamelContext()
        this.dynamicRouteManager = new DynamicRouteManager(camelContext)

        // Configure basic settings
        camelContext.setTracing(false)
        camelContext.setMessageHistory(true)
        camelContext.setLoadTypeConverters(true)
    }


    private CompletableFuture startCamelFlow (Map variables = [:]) {
        CompletableFuture result =  CompletableFuture.supplyAsync {
            try {
                camelContext.start()

                def results = [:]
                resultFutures.each { routeId, future ->
                    try {
                        results[routeId] = future.get()
                    } catch (Exception e) {
                        log.error("Error executing route: ${routeId}", e)
                        future.completeExceptionally(e)
                    }
                }

                return results
            } catch (Exception e) {
                log.error("Error during execution", e)
                throw e
            } finally {
                shutdownCamelContext()
            }
        }
        return result
    }

    def addRoute(RouteConfig config, @DelegatesTo(RouteBuilder) Closure closure) {
        resultFutures[config.routeId] = new CompletableFuture<>()
        dynamicRouteManager.addRoute(config, closure)
        return this
    }

    def shutdownCamelContext() {
        if (camelContext?.started) {
            try {
                camelContext.stop()
            } catch (Exception e) {
                log.error("Error shutting down Camel context", e)
            }
        }
    }

}

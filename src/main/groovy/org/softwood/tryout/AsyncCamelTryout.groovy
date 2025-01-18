package org.softwood.tryout

import groovy.util.logging.Slf4j
import org.apache.camel.CamelContext
import org.apache.camel.Exchange
import org.softwood.taskTypes.CamelFlowTask
import org.softwood.taskTypes.CircuitBreakerConfig
import org.softwood.taskTypes.RouteConfig
import org.slf4j.LoggerFactory

def log = LoggerFactory.getLogger(this.class)


def context = SpringContextUtils.initialise("dev", ["org.softwood.processEngine", "org.softwood.processLibrary", "org.softwood.processBeanConfiguration"])
def beanFactories = context.getBeanFactory()
def camelContext = SpringContextUtils.getBean ('camelConfig')

// Example usage:
def task = new CamelFlowTask()


def routeConfig = RouteConfig.builder()
        .routeId("test-route")
        .fromEndpoint("direct:start")
        .resultEndpoint("direct:end")
        .retryAttempts(3)
        .circuitBreaker(
                CircuitBreakerConfig.builder()
                        .failureThreshold(3)
                        .resetTimeout(5000)
                        .fallback { Exchange exchange ->
                            exchange.message.body = "Fallback Response: couldnt make route work"
                        }
                        .build()
        )
        .errorHandler { Exchange exchange ->
            log.error ("Error in route: ${exchange.getProperty(Exchange.EXCEPTION_CAUGHT)}")
        }
        .build()

task.addRoute(routeConfig) { Exchange exchange ->
    from("direct:start")
            .transform().body { it.toString().toUpperCase() }
            .to("direct:end")
}

def result = task.runTask().thenApply { results ->
    println ("Route results: $results")
    return results
}.exceptionally { throwable ->
    println ("Error occurred", throwable)
    return null
}


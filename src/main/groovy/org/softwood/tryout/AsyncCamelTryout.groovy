package org.softwood.tryout

import groovy.util.logging.Slf4j
import org.apache.camel.CamelContext
import org.apache.camel.Exchange
import org.apache.camel.ProducerTemplate
import org.softwood.taskTypes.CamelFlowTask
import org.softwood.taskTypes.CircuitBreakerConfig
import org.softwood.taskTypes.ExecutableTaskTrait
import org.softwood.taskTypes.RouteConfig
import org.slf4j.LoggerFactory

import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException
import java.util.function.Function

def log = LoggerFactory.getLogger(this.class)


def context = SpringContextUtils.initialise("dev", ["org.softwood.processEngine", "org.softwood.processLibrary", "org.softwood.processBeanConfiguration"])
def beanFactories = context.getBeanFactory()
def camelContext = SpringContextUtils.getBean ('camelConfig')

// Example usage:
ExecutableTaskTrait task = new CamelFlowTask()


def routeConfig = RouteConfig.builder().with {
        routeId("test-route")
        fromEndpoint("direct:start")
        resultEndpoint("direct:end")
        retryAttempts(3)
        circuitBreaker(
                CircuitBreakerConfig.builder().with {
                        failureThreshold(3)
                        resetTimeout(5000)
                        halfOpenAttempts(3)
                        fallback { Exchange exchange ->
                            exchange.message.body = "Fallback Response: couldn't make route work"
                        }
                        build()
                }  )
        errorHandler { Exchange exchange ->
            log.error ("Error in route: ${exchange.getProperty(Exchange.EXCEPTION_CAUGHT)}")
        }
        build()
    }

task.addRoute(routeConfig) { Exchange exchange ->
    Function tx = {log.info "got body $it from exchange"
        it.toString().toUpperCase()
    }
    from("direct:start")
            .log("simple start-end route started ")
            .transform().body (tx)
            .to("direct:end")
}

def result = task.execute()


try {
    def ans = result.get(5, TimeUnit.SECONDS)
    log.info "read '$ans' from the camel task future  "
} catch (TimeoutException to) {
    log.info "result of camel task future timed out "
}

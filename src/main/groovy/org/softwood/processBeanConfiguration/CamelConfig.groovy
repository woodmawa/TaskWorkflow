package org.softwood.processBeanConfiguration

import groovy.util.logging.Slf4j
import org.apache.camel.CamelConfiguration
import org.apache.camel.CamelContext
import org.apache.camel.Component
import org.apache.camel.ConsumerTemplate
import org.apache.camel.ContextEvents
import org.apache.camel.Endpoint
import org.apache.camel.ExtendedCamelContext
import org.apache.camel.FluentProducerTemplate
import org.apache.camel.GlobalEndpointConfiguration
import org.apache.camel.NoSuchLanguageException
import org.apache.camel.Processor
import org.apache.camel.ProducerTemplate
import org.apache.camel.Route
import org.apache.camel.RouteConfigurationsBuilder
import org.apache.camel.RouteTemplateContext
import org.apache.camel.RoutesBuilder
import org.apache.camel.Service
import org.apache.camel.ServiceStatus
import org.apache.camel.ShutdownRoute
import org.apache.camel.ShutdownRunningTask
import org.apache.camel.StartupListener
import org.apache.camel.StartupSummaryLevel
import org.apache.camel.TypeConverter
import org.apache.camel.builder.RouteBuilder
import org.apache.camel.clock.EventClock
import org.apache.camel.impl.DefaultCamelContext
import org.apache.camel.language.bean.Bean
import org.apache.camel.spi.CamelContextNameStrategy
import org.apache.camel.spi.ClassResolver
import org.apache.camel.spi.DataFormat
import org.apache.camel.spi.DataType
import org.apache.camel.spi.Debugger
import org.apache.camel.spi.EndpointRegistry
import org.apache.camel.spi.ExecutorServiceManager
import org.apache.camel.spi.InflightRepository
import org.apache.camel.spi.Injector
import org.apache.camel.spi.Language
import org.apache.camel.spi.LifecycleStrategy
import org.apache.camel.spi.ManagementNameStrategy
import org.apache.camel.spi.ManagementStrategy
import org.apache.camel.spi.MessageHistoryFactory
import org.apache.camel.spi.PropertiesComponent
import org.apache.camel.spi.Registry
import org.apache.camel.spi.RestConfiguration
import org.apache.camel.spi.RestRegistry
import org.apache.camel.spi.RouteController
import org.apache.camel.spi.RoutePolicyFactory
import org.apache.camel.spi.RuntimeEndpointRegistry
import org.apache.camel.spi.ShutdownStrategy
import org.apache.camel.spi.StreamCachingStrategy
import org.apache.camel.spi.Tracer
import org.apache.camel.spi.Transformer
import org.apache.camel.spi.TransformerRegistry
import org.apache.camel.spi.TypeConverterRegistry
import org.apache.camel.spi.UuidGenerator
import org.apache.camel.spi.Validator
import org.apache.camel.spi.ValidatorRegistry
import org.apache.camel.support.jsse.SSLContextParameters
import org.apache.camel.vault.VaultConfiguration
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Scope

import java.time.Duration
import java.util.function.Predicate

@Slf4j
@Configuration
class CamelConfig  {
    @Bean
    @Scope ("prototype")
    CamelContext camelContext () {
        log.info "create camel context "
        CamelContext camelCtx = new DefaultCamelContext()
        RouteBuilder route = {it -> from ('direct:start')
                .to('log:received')} as RouteBuilder
        camelCtx.addRoutes (route)
        return camelCtx
    }

}

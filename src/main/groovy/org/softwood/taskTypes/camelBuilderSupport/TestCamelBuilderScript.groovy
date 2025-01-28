package org.softwood.taskTypes.camelBuilderSupport

import org.apache.camel.CamelContext
import org.apache.camel.Exchange
import org.apache.camel.component.jms.JmsComponent
import org.apache.camel.component.file.FileComponent
import org.apache.camel.component.stream.StreamComponent

def builder = new CamelBuilder()

builder.with {
    components {
        //methodMissing handles the stated component names
        file(new org.apache.camel.component.file.FileComponent())
        jms {
            JmsComponent component = new JmsComponent()
            component.createConnectionFactory()
            return component
        }
        stream {
            StreamComponent sc = new StreamComponent()
        }
    }

    route {
        from("direct:start")
            .choice()
                .when { exchange ->
                    exchange.in.body.toString().contains("file")
                }
                    .to("file:output")
                .when { exchange ->
                    exchange.in.body.toString().contains("jms")
                }
                    .to("jms:queue:orders")
                    .process { exchange ->
                        log.info("Sent to JMS: ${exchange.in.body}")
                    }
                .otherwise()
                    .to("direct:defaultHandler")
            .endChoice()
    }

    route {
        from("direct:defaultHandler")
                .process { exchange ->
                    exchange.in.body = "Processed: ${exchange.in.body}"
                }
                .to("file:default-output")
    }

    route {
        from("direct:errorHandler")
                .process { exchange ->
                    def exception = exchange.getProperty(Exchange.EXCEPTION_CAUGHT, Exception.class)
                    log.error("Error processing: ${exception.message}")
                }
                .to("file:error-output")
    }
}


CamelContext context = builder.build()
context.createProducerTemplate().
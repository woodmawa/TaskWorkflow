package org.softwood.taskTypes.camelBuilderSupport

import org.apache.camel.CamelContext
import org.apache.camel.Exchange
import org.apache.camel.ProducerTemplate
import org.apache.camel.component.bean.BeanComponent
import org.apache.camel.component.beanclass.ClassComponent
import org.apache.camel.component.jms.JmsComponent
import org.apache.camel.component.file.FileComponent
import org.apache.camel.component.log.LogComponent
import org.apache.camel.component.stream.StreamComponent

def builder = new CamelBuilder()

builder.with {
    components {
        //methodMissing handles the stated component names
        file(new org.apache.camel.component.file.FileComponent())
        jms {
            JmsComponent component = new JmsComponent()
            //component.createConnectionFactory()
            return component
        }
        stream {
            StreamComponent sc = new StreamComponent()
        }
        bean {
            BeanComponent bc = new BeanComponent()
            def scope = bc.scope
            bc
        }
        clazz (new ClassComponent())
        logger (new LogComponent())
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
        def cb = it
        def cur = this
        def del = delegate
        def own = owner
        def par = owner.delegate
        def res = par.from ("direct:defaultHandler" )
        def fr = from("direct:defaultHandler")
                fr.xxtransform { exchange ->
                    String body = exchange.in.body
                    log.info "Processed orig input : ${body}"
                    exchange.in.body = body.toUpperCase()  //relay the message to stream:out
                }
                .to ("log:org.softwood.taskTypes.camelBuilderSupport.TestCamelBuilderScript?level=INFO")
                .to ("class:org.softwood.taskTypes.camelBuilderSupport.TestClassBean?method=testHello")
                .to("stream:out")
    }

    route {
        from("direct:errorHandler")
                .process { exchange ->
                    def exception = exchange.getProperty(Exchange.EXCEPTION_CAUGHT, Exception.class)
                    log.error("Error processing: ${exception.message}")
                }
                .to("stream:out")
    }
}


CamelContext context = builder.build()
ProducerTemplate trigger = context.createProducerTemplate()
trigger.sendBody("direct:start","any old text ")
sleep(1000)
context.stop()
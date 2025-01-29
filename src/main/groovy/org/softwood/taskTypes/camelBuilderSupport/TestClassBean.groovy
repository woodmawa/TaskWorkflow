package org.softwood.taskTypes.camelBuilderSupport

/*
simple class lookup and turn into bean using camels 'class' endpoint
 */
class TestClassBean {
    def testHello (def body ){
        println "\t--> class method invoked by camel with $body"
        return body  //make sure we return a result to the exchange
    }
}

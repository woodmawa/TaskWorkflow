package org.softwood.tryout.tryOutTrait

import java.util.concurrent.CompletableFuture
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.stream.Stream

List values = [1,2]
List<CompletableFuture> futures = []
CountDownLatch latch = new CountDownLatch(2)

futures = values.collect { def fut = CompletableFuture.supplyAsync {->
    println "it value was $it "
    Thread.sleep(2);
    latch.countDown()
    println "reduced latch count to $latch.count"
    it}
}

/*def latchCompleted = latch.await(10, TimeUnit.MILLISECONDS)
assert latch.count == 0
println "latchCompleted as expected $latchCompleted"  //will be false if timeout fires
assert latchCompleted == true */

println futures  //has two entries but not completed at this point

//sleep(10)  //give it time to complete the futures ...

//problem to solve with the latch is when is knowing when the the futures list is full with all the results its going to get
def result, result2

//this is called in non blocking way
CompletableFuture.allOf(*futures).thenAccept {println "thenAccept.."; result = futures.collect{it.get()}}

//do a join on the AllOf  - waits for that to return - means i dont need the sleep call above ..
CompletableFuture.allOf(*futures).join()
Stream.of(*futures)
        .map(CompletableFuture::join)
        .forEach( entry -> println "got entry $entry")

CompletableFuture.allOf(*futures).thenApply {println "thenAccept and do join in result2 .."; result2 = futures.collect{it.join()}}
//maybe join wants to block until the results are in ??


println "result as $result and result2 as $result2"
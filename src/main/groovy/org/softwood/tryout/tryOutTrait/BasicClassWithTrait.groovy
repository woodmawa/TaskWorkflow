package org.softwood.tryout.tryOutTrait

import groovy.util.logging.Slf4j

@Slf4j
class BasicClassWithTrait implements TalkingTrait {
    String name

    def getFromTrait() {
        TalkingTrait.super.talk()
    }

    def getSuper() {
        TalkingTrait.super
    }
}

package org.softwood.tryout.tryOutTrait

import groovy.util.logging.Slf4j

@Slf4j
trait TalkingTrait {

    String trait_name

    def self () {
        return this
    }

    def tsup () {

        //return "talk super:  " + TalkingTrait.super
    }

    def getTalk () {
        "talk blah..."
    }

    String talk () {
        log.info "talk: from trait $trait_name and class $name"
        "talk: talk done "
    }
}
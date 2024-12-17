package org.softwood.gatewayTypes

trait ConditionalGatewayTrait extends GatewayTrait {
    def evaluateConditions (def value) {
        this.evaluateConditions(value)
    }

    void setConditionsMap (Map conditionsMap ) {
        this.conditionsMap = conditionsMap ?: [:]
    }

    def getConditionsEvalutionResults () {
        this.conditionsEvalutionResults
    }
}
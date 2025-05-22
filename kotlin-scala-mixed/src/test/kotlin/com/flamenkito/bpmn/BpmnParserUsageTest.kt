package com.flamenkito.bpmn

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.io.File

class BpmnParserUsageTest {

    @Test
    fun `should parse user registration process and verify structure`() {
        // given
        val parser = BpmnParser()
        val bpmnFile = File(javaClass.getResource("/bpmn/user-registration.bpmn")!!.file)

        // when
        val processes = parser.parse(bpmnFile)

        // then
        assertThat(processes).hasSize(1)
        val process = processes.first()

        // verify basic process info
        assertThat(process.id).isEqualTo("user_registration_process")
        assertThat(process.name).isEqualTo("User Registration Process")

        // verify flow structure
        assertThat(process.elements).hasSize(8) // all tasks, events and gateways
        assertThat(process.sequenceFlows).hasSize(9) // all connections

        // verify that we have proper gateway with conditions
        val validationGateway = process.elements.find { it.id == "check_validation" }
        assertThat(validationGateway).isNotNull

        // verify conditional flows
        val conditionalFlows = process.sequenceFlows.filter { it.condition != null }
        assertThat(conditionalFlows).hasSize(2)
        assertThat(conditionalFlows.map { it.id }).containsExactlyInAnyOrder(
            "flow_valid_data",
            "flow_invalid_data"
        )
    }
}

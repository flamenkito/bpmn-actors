package com.flamenkito.bpmn

import com.flamenkito.bpmn.ast.*
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.io.File

class BpmnParserTest {
    private val parser = BpmnParser()
    private lateinit var bpmnFile: File

    @BeforeEach
    fun setUp() {
        val resource = javaClass.getResourceAsStream("/bpmn/user-registration.bpmn")
            ?: throw IllegalStateException("Test BPMN file not found")
        bpmnFile = File.createTempFile("test", ".bpmn").apply {
            deleteOnExit()
            outputStream().use { out ->
                resource.use { it.copyTo(out) }
            }
        }
    }

    @Test
    fun `should parse user registration process correctly`() {
        // when
        val processes = parser.parse(bpmnFile)

        // then
        assertThat(processes).hasSize(1)
        val process = processes.first()

        // verify basic process info
        assertThat(process.id).isEqualTo("user_registration_process")
        assertThat(process.name).isEqualTo("User Registration Process")

        // verify flow structure
        assertThat(process.elements).hasSize(10) // all tasks, events and gateways
        assertThat(process.sequenceFlows).hasSize(9) // all connections

        // verify start event
        val startEvent = process.elements.filterIsInstance<StartEvent>().first()
        assertThat(startEvent.id).isEqualTo("start_registration")
        assertThat(startEvent.name).isEqualTo("Registration Started")

        // verify user tasks
        val userTasks = process.elements.filterIsInstance<Task>()
            .filter { it.type == TaskType.USER_TASK }
        assertThat(userTasks).hasSize(2)
        assertThat(userTasks.map { it.id }).containsExactlyInAnyOrder(
            "fill_registration_form",
            "verify_email"
        )

        // verify service tasks
        val serviceTasks = process.elements.filterIsInstance<Task>()
            .filter { it.type == TaskType.SERVICE_TASK }
        assertThat(serviceTasks).hasSize(4)
        assertThat(serviceTasks.map { it.id }).containsExactlyInAnyOrder(
            "validate_user_data",
            "send_verification_email",
            "create_user_account",
            "send_welcome_email"
        )

        // verify gateway
        val gateways = process.elements.filterIsInstance<Gateway>()
        assertThat(gateways).hasSize(1)
        val gateway = gateways.first()
        assertThat(gateway.id).isEqualTo("check_validation")
        assertThat(gateway.type).isEqualTo(GatewayType.EXCLUSIVE)

        // verify conditional flows
        val conditionalFlows = process.sequenceFlows.filter { it.condition != null }
        assertThat(conditionalFlows).hasSize(2)
        assertThat(conditionalFlows.map { it.id }).containsExactlyInAnyOrder(
            "flow_valid_data",
            "flow_invalid_data"
        )
    }
}

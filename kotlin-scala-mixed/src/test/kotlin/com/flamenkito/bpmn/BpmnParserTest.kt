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
    fun `should parse user registration process`() {
        // when
        val processes = parser.parse(bpmnFile)

        // then
        assertThat(processes).hasSize(1)
        val process = processes.first()

        // verify process metadata
        assertThat(process.id).isEqualTo("user_registration_process")
        assertThat(process.name).isEqualTo("User Registration Process")

        // verify flow nodes
        val nodes = process.elements
        assertThat(nodes).hasSize(8)

        // verify start event
        val startEvent = nodes.filterIsInstance<StartEvent>().first()
        assertThat(startEvent.id).isEqualTo("start_registration")
        assertThat(startEvent.name).isEqualTo("Registration Started")

        // verify user tasks
        val userTasks = nodes.filterIsInstance<Task>()
            .filter { it.type == TaskType.USER_TASK }
        assertThat(userTasks).hasSize(2)
        assertThat(userTasks.map { it.id }).containsExactlyInAnyOrder(
            "fill_registration_form",
            "verify_email"
        )

        // verify service tasks
        val serviceTasks = nodes.filterIsInstance<Task>()
            .filter { it.type == TaskType.SERVICE_TASK }
        assertThat(serviceTasks).hasSize(4)
        assertThat(serviceTasks.map { it.id }).containsExactlyInAnyOrder(
            "validate_user_data",
            "send_verification_email",
            "create_user_account",
            "send_welcome_email"
        )

        // verify gateway
        val gateways = nodes.filterIsInstance<Gateway>()
        assertThat(gateways).hasSize(1)
        val gateway = gateways.first()
        assertThat(gateway.id).isEqualTo("check_validation")
        assertThat(gateway.type).isEqualTo(GatewayType.EXCLUSIVE)

        // verify sequence flows
        val flows = process.sequenceFlows
        assertThat(flows).hasSize(9)

        // verify conditional flows
        val conditionalFlows = flows.filter { it.condition != null }
        assertThat(conditionalFlows).hasSize(2)
        assertThat(conditionalFlows.map { it.id }).containsExactlyInAnyOrder(
            "flow_valid_data",
            "flow_invalid_data"
        )

        // verify complete happy path
        val happyPath = listOf(
            "start_registration",
            "fill_registration_form",
            "validate_user_data",
            "check_validation",
            "send_verification_email",
            "verify_email",
            "create_user_account",
            "send_welcome_email",
            "registration_completed"
        )

        happyPath.windowed(2).forEach { (source, target) ->
            assertThat(flows.any { it.sourceRef == source && it.targetRef == target })
                .withFailMessage("Missing flow from $source to $target")
                .isTrue()
        }
    }
}

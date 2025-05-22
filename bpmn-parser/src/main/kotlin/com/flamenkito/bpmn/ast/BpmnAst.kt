package com.flamenkito.bpmn.ast

sealed interface BpmnNode {
    val id: String
    val name: String?
}

data class BpmnProcess(
    override val id: String,
    override val name: String?,
    val elements: List<FlowNode>,
    val sequenceFlows: List<SequenceFlow>
) : BpmnNode

sealed interface FlowNode : BpmnNode

data class StartEvent(
    override val id: String,
    override val name: String?
) : FlowNode

data class EndEvent(
    override val id: String,
    override val name: String?
) : FlowNode

data class Task(
    override val id: String,
    override val name: String?,
    val type: TaskType
) : FlowNode

data class Gateway(
    override val id: String,
    override val name: String?,
    val type: GatewayType
) : FlowNode

data class SequenceFlow(
    override val id: String,
    override val name: String?,
    val sourceRef: String,
    val targetRef: String,
    val condition: String?
) : BpmnNode

enum class TaskType {
    USER_TASK,
    SERVICE_TASK,
    SCRIPT_TASK,
    MANUAL_TASK
}

enum class GatewayType {
    EXCLUSIVE,
    PARALLEL,
    INCLUSIVE,
    EVENT_BASED
}

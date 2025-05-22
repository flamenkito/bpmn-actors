package com.flamenkito.bpmn

import com.flamenkito.bpmn.ast.*
import org.camunda.bpm.model.bpmn.Bpmn
import org.camunda.bpm.model.bpmn.instance.*
import java.io.File
import java.io.InputStream

class BpmnParser {
    fun parse(file: File): List<BpmnProcess> = parse(file.inputStream())

    fun parse(inputStream: InputStream): List<BpmnProcess> {
        val modelInstance = Bpmn.readModelFromStream(inputStream)
        return modelInstance.getModelElementsByType(Process::class.java).map { process ->
            BpmnProcess(
                id = process.id,
                name = process.name,
                elements = parseFlowNodes(process),
                sequenceFlows = parseSequenceFlows(process)
            )
        }
    }

    private fun parseFlowNodes(process: Process): List<FlowNode> {
        return process.flowElements
            .filterIsInstance<org.camunda.bpm.model.bpmn.instance.FlowNode>()
            .mapNotNull { node ->
                when (node) {
                    is StartEvent -> StartEvent(
                        id = node.id,
                        name = node.name
                    )
                    is EndEvent -> EndEvent(
                        id = node.id,
                        name = node.name
                    )
                    is ServiceTask -> Task(
                        id = node.id,
                        name = node.name,
                        type = TaskType.SERVICE_TASK
                    )
                    is UserTask -> Task(
                        id = node.id,
                        name = node.name,
                        type = TaskType.USER_TASK
                    )
                    is ScriptTask -> Task(
                        id = node.id,
                        name = node.name,
                        type = TaskType.SCRIPT_TASK
                    )
                    is ManualTask -> Task(
                        id = node.id,
                        name = node.name,
                        type = TaskType.MANUAL_TASK
                    )
                    is ExclusiveGateway -> Gateway(
                        id = node.id,
                        name = node.name,
                        type = GatewayType.EXCLUSIVE
                    )
                    is ParallelGateway -> Gateway(
                        id = node.id,
                        name = node.name,
                        type = GatewayType.PARALLEL
                    )
                    is InclusiveGateway -> Gateway(
                        id = node.id,
                        name = node.name,
                        type = GatewayType.INCLUSIVE
                    )
                    is EventBasedGateway -> Gateway(
                        id = node.id,
                        name = node.name,
                        type = GatewayType.EVENT_BASED
                    )
                    else -> null
                }
            }
    }

    private fun parseSequenceFlows(process: Process): List<SequenceFlow> {
        return process.flowElements
            .filterIsInstance<org.camunda.bpm.model.bpmn.instance.SequenceFlow>()
            .map { flow ->
                SequenceFlow(
                    id = flow.id,
                    name = flow.name,
                    sourceRef = flow.source.id,
                    targetRef = flow.target.id,
                    condition = flow.conditionExpression?.textContent
                )
            }
    }
}

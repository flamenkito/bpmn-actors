package com.flamenkito.bpmn

import java.io.File

fun main(args: Array<String>) {
    if (args.isEmpty()) {
        println("Usage: <bpmn-file-path>")
        return
    }
    val bpmnFile = File(args[0])
    if (!bpmnFile.exists()) {
        println("File not found: ${args[0]}")
        return
    }
    val parser = BpmnParser()
    parser.parseAndWriteJson(bpmnFile)
    println("AST JSON written to: ${bpmnFile.parentFile.resolve(bpmnFile.nameWithoutExtension + ".json")}")
}


package com.sgffsg.example.asm

import org.gradle.api.Plugin
import org.gradle.api.Project

class MethodStatPlugin implements Plugin<Project> {

    @Override
    void apply(Project target) {
        println("Hello from plugin 'com.example.plugin.MethodStatPlugin'")
//        project.tasks.register("testTask") {
//            doLast {
//                println("Hello from plugin 'com.example.plugin.customname'")
//            }
//        }
    }
}
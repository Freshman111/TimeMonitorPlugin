package com.sgffsg.example.asm

import com.sgffsg.example.asm.transform.ActivityOnCreateTransform
import com.sgffsg.example.asm.transform.MethodTimeTransform
import com.sgffsg.example.asm.transform.SystemTraceTransform
import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project

class MethodStatPlugin implements Plugin<Project> {

    //简单使用，跑通自定义插件
//    @Override
//    void apply(Project target) {
//        println("Hello from plugin 'com.example.plugin.MethodStatPlugin'")
//    }

    //使用ASM demo1
    @Override
    void apply(Project target) {
        println("Hello from plugin 'com.example.plugin.MethodStatPlugin'")
//        target.android.registerTransform(new ActivityOnCreateTransform(target))
    }

    //使用ASM demo2
//    @Override
//    void apply(Project target) {
//        println("Hello from plugin 'com.example.plugin.MethodStatPlugin'")
//        target.android.registerTransform(new MethodTimeTransform(target))
//    }

    //使用ASM demo3
//    @Override
//    void apply(Project target) {
//        project.extensions.create("systrace", SystraceExtension)
//
//        if (!project.plugins.hasPlugin('com.android.application')) {
//            throw new GradleException('Systrace Plugin, Android Application plugin required')
//        }
//
//        project.afterEvaluate {
//            def android = project.extensions.android
//            def configuration = project.systrace
//            android.applicationVariants.all { variant ->
//
//                String output = configuration.output
//                if (Util.isNullOrNil(output)) {
//                    configuration.output = project.getBuildDir().getAbsolutePath() + File.separator + "systrace_output"
//                    Log.i(TAG, "set Systrace output file to " + configuration.output)
//                }
//
//                Log.i(TAG, "Trace enable is %s", configuration.enable)
//                if (configuration.enable) {
//                    SystemTraceTransform.inject(project, variant)
//                }
//            }
//        }
//    }
}
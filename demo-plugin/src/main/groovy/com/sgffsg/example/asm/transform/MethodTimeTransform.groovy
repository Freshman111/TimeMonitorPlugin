package com.sgffsg.example.asm.transform

import com.android.build.api.transform.DirectoryInput
import com.android.build.api.transform.Format
import com.android.build.api.transform.JarInput
import com.android.build.api.transform.QualifiedContent
import com.android.build.api.transform.Transform
import com.android.build.api.transform.TransformException
import com.android.build.api.transform.TransformInvocation
import com.android.build.api.transform.TransformOutputProvider
import com.android.build.gradle.internal.dependency.CustomClassVisitor
import com.android.utils.FileUtils
import groovyjarjarasm.asm.ClassReader
import groovyjarjarasm.asm.ClassVisitor
import groovyjarjarasm.asm.ClassWriter
import org.apache.tools.ant.Project

class MethodTimeTransform extends Transform {

    private Project mProject

    MethodTimeTransform(Project project) {
        this.mProject = project
    }

    @Override
    String getName() {
        return "MethodTimeTransform"
    }

    @Override
    Set<QualifiedContent.ContentType> getInputTypes() {
        return TransformManager.CONTENT_CLASS
    }

    @Override
    Set<? super QualifiedContent.Scope> getScopes() {
        return TransformManager.SCOPE_FULL_PROJECT
    }

    @Override
    boolean isIncremental() {
        return false
    }

    @Override
    void transform(TransformInvocation transformInvocation) throws TransformException, InterruptedException, IOException {
        println("//============asm visit start===============//")
        def startTime = System.currentTimeMillis()

        TransformOutputProvider outputProvider = transformInvocation.outputProvider
        if (outputProvider != null) {
            outputProvider.deleteAll()
        }

        transformInvocation.inputs.each { input ->


            input.directoryInputs.each { DirectoryInput directoryInput ->
                handleDirectoryInput(directoryInput, outputProvider)
            }
            //input分为两类：一个是项目中的，一个是jar包中的。我们目前只处理项目中的。
//            input.jarInputs.each { JarInput jarInput ->
//                handleJarInput(jarInput, outputProvider)
//            }
        }

        def customTime = (System.currentTimeMillis() - startTime) / 1000
        println("MethodTimeTransform plugin custom time = " + customTime + " s")
        println("//============asm visit end===============//")

    }

    static void handleDirectoryInput(DirectoryInput directoryInput, TransformOutputProvider outputProvider) {
        if (directoryInput.file.isDirectory()) {
            directoryInput.file.eachFileRecurse { File file ->

                def name = file.name
                // 排除不需要修改的类
                if (name.endsWith(".class") && !name.startsWith("R\$") && !"R.class".equals(name) && !"BuildConfig.class".equals(name)) {
                    println("name =="+ name + "===is changing...")
                    ClassReader classReader = new ClassReader(file.bytes)
                    //
                    ClassWriter classWriter = new ClassWriter(classReader, ClassWriter.COMPUTE_MAXS)
                    //
                    ClassVisitor classVisitor = new CustomClassVisitor(classWriter)

                    classReader.accept(classVisitor, EXPAND_FRAMES)

                    byte [] code = classWriter.toByteArray()

                    FileOutputStream fos = new FileOutputStream(file.parentFile.absolutePath + File.separator + name)

                    fos.write(code)

                    fos.close()
                }

            }
        }

        //处理完输入文件之后，要把输出给下一个任务
        def dest = outputProvider.getContentLocation(directoryInput.name, directoryInput.contentTypes, directoryInput.scopes, Format.DIRECTORY)

        FileUtils.copyDirectory(directoryInput.file, dest)
    }


}
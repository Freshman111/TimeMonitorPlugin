package com.sgffsg.example.asm.transform

import com.android.build.api.transform.*
import com.android.utils.FileUtils
import com.sgffsg.example.demo_plugin.TraceVisitor
import groovyjarjarasm.asm.ClassReader
import groovyjarjarasm.asm.ClassVisitor
import groovyjarjarasm.asm.ClassWriter
import org.gradle.api.Project

/**
 * 拦截所有的Activity的onCreate()方法，并打印onCreate方法的耗时
 * learn from https://juejin.cn/post/6844903573331902472
 */
class ActivityOnCreateTransform extends Transform {

    private Project mProject

    ActivityOnCreateTransform(Project project) {
        this.mProject = project
    }

    @Override
    String getName() {
        return "ActivityOnCreateTransform"
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
    Object getProperty(String propertyName) {
        return super.getProperty(propertyName)
    }

    @Override
    boolean isIncremental() {
        return false
    }

    @Override
    public void transform(TransformInvocation transformInvocation) throws TransformException, InterruptedException, IOException {
        println("//============asm transform start===============//")
        TransformOutputProvider outputProvider = transformInvocation.outputProvider
        //删除之前的输出
        if (outputProvider != null) {
            outputProvider.deleteAll()
        }
        //遍历inputs里的TransformInput
        transformInvocation.inputs.each { TransformInput input ->
            //遍历input里边的DirectoryInput
            input.directoryInputs.each {
                DirectoryInput directoryInput ->
                    //是否是目录
                    if (directoryInput.file.isDirectory()) {
                        //遍历目录
                        directoryInput.file.eachFileRecurse {
                            File file ->
                                def filename = file.name
                                def name = file.name
                                //这里进行我们的处理 TODO
                                if (name.endsWith(".class") && !name.startsWith("R\$") &&
                                        "R.class" != name && "BuildConfig.class" != name) {
                                    ClassReader classReader = new ClassReader(file.bytes)
                                    ClassWriter classWriter = new ClassWriter(classReader, ClassWriter.COMPUTE_MAXS)
                                    def className = name.split(".class")[0]
                                    ClassVisitor cv = new TraceVisitor(className, classWriter)
                                    classReader.accept(cv, EXPAND_FRAMES)
                                    byte[] code = classWriter.toByteArray()
                                    FileOutputStream fos = new FileOutputStream(
                                            file.parentFile.absolutePath + File.separator + name)
                                    fos.write(code)
                                    fos.close()

                                }
                        }
                    }
                    //处理完输入文件之后，要把输出给下一个任务
                    def dest = outputProvider.getContentLocation(directoryInput.name,
                            directoryInput.contentTypes, directoryInput.scopes,
                            Format.DIRECTORY)
                    FileUtils.copyDirectory(directoryInput.file, dest)
            }

        }
            //暂时不处理jar包里面的
//            input.jarInputs.each { JarInput jarInput ->
//                /**
//                 * 重名名输出文件,因为可能同名,会覆盖
//                 */
//                def jarName = jarInput.name
//                def md5Name = DigestUtils.md5Hex(jarInput.file.getAbsolutePath())
//                if (jarName.endsWith(".jar")) {
//                    jarName = jarName.substring(0, jarName.length() - 4)
//                }
//
//                File tmpFile = null;
//                if (jarInput.file.getAbsolutePath().endsWith(".jar")) {
//                    JarFile jarFile = new JarFile(jarInput.file);
//                    Enumeration enumeration = jarFile.entries();
//                    tmpFile = new File(jarInput.file.getParent() + File.separator + "classes_trace.jar");
//                    //避免上次的缓存被重复插入
//                    if (tmpFile.exists()) {
//                        tmpFile.delete();
//                    }
//                    JarOutputStream jarOutputStream = new JarOutputStream(new FileOutputStream(tmpFile));
//                    //用于保存
//                    ArrayList<String> processorList = new ArrayList<>();
//                    while (enumeration.hasMoreElements()) {
//                        JarEntry jarEntry = (JarEntry) enumeration.nextElement();
//                        String entryName = jarEntry.getName();
//                        ZipEntry zipEntry = new ZipEntry(entryName);
//                        //println "MeetyouCost entryName :" + entryName
//                        InputStream inputStream = jarFile.getInputStream(jarEntry);
//                        //如果是inject文件就跳过
//
//                        //重点:插桩class
//                        if (entryName.endsWith(".class") && !entryName.contains("R\$") &&
//                                !entryName.contains("R.class") && !entryName.contains("BuildConfig.class")) {
//                            //class文件处理
//                            jarOutputStream.putNextEntry(zipEntry);
//                            ClassReader classReader = new ClassReader(IOUtils.toByteArray(inputStream))
//                            ClassWriter classWriter = new ClassWriter(classReader, ClassWriter.COMPUTE_MAXS)
//                            def className = entryName.split(".class")[0]
//                            ClassVisitor cv = new TraceVisitor(className, classWriter)
//                            classReader.accept(cv, EXPAND_FRAMES)
//                            byte[] code = classWriter.toByteArray()
//                            jarOutputStream.write(code)
//
//                        } else if (entryName.contains("META-INF/services/javax.annotation.processing.Processor")) {
//                            if (!processorList.contains(entryName)) {
//                                processorList.add(entryName)
//                                jarOutputStream.putNextEntry(zipEntry);
//                                jarOutputStream.write(IOUtils.toByteArray(inputStream));
//                            } else {
//                                println "duplicate entry:" + entryName
//                            }
//                        } else {
//
//                            jarOutputStream.putNextEntry(zipEntry);
//                            jarOutputStream.write(IOUtils.toByteArray(inputStream));
//                        }
//
//                        jarOutputStream.closeEntry();
//                    }
//                    //写入inject注解
//
//                    //结束
//                    jarOutputStream.close();
//                    jarFile.close();
//                }
//
//                //处理jar进行字节码注入处理 TODO
//
//                def dest = outputProvider.getContentLocation(jarName + md5Name,
//                        jarInput.contentTypes, jarInput.scopes, Format.JAR)
//                if (tmpFile == null) {
//                    FileUtils.copyFile(jarInput.file, dest)
//                } else {
//                    FileUtils.copyFile(tmpFile, dest)
//                    tmpFile.delete()
//                }
//            }
        println("//============asm transform end===============//")
    }


    //已经被弃用了
    @Override
    void transform(Context context, Collection<TransformInput> inputs, Collection<TransformInput> referencedInputs, TransformOutputProvider outputProvider, boolean isIncremental) throws IOException, TransformException, InterruptedException {
        println("//============asm transform start===============//")
        //删除之前的输出
        if (outputProvider != null)
            outputProvider.deleteAll()
        //遍历inputs里的TransformInput
        inputs.each { TransformInput input ->
            //遍历input里边的DirectoryInput
            input.directoryInputs.each {
                DirectoryInput directoryInput ->
                    //是否是目录
                    if (directoryInput.file.isDirectory()) {
                        //遍历目录
                        directoryInput.file.eachFileRecurse {
                            File file ->
                                def filename = file.name
                                def name = file.name
                                //这里进行我们的处理 TODO
                                if (name.endsWith(".class") && !name.startsWith("R\$") &&
                                        "R.class" != name && "BuildConfig.class" != name) {
                                    ClassReader classReader = new ClassReader(file.bytes)
                                    ClassWriter classWriter = new ClassWriter(classReader, ClassWriter.COMPUTE_MAXS)
                                    def className = name.split(".class")[0]
                                    ClassVisitor cv = new TraceVisitor(className, classWriter)
                                    classReader.accept(cv, EXPAND_FRAMES)
                                    byte[] code = classWriter.toByteArray()
                                    FileOutputStream fos = new FileOutputStream(
                                            file.parentFile.absolutePath + File.separator + name)
                                    fos.write(code)
                                    fos.close()

                                }
                        }
                    }
                    //处理完输入文件之后，要把输出给下一个任务
                    def dest = outputProvider.getContentLocation(directoryInput.name,
                            directoryInput.contentTypes, directoryInput.scopes,
                            Format.DIRECTORY)
                    FileUtils.copyDirectory(directoryInput.file, dest)
            }

            //暂时不处理jar包里面的
//            input.jarInputs.each { JarInput jarInput ->
//                /**
//                 * 重名名输出文件,因为可能同名,会覆盖
//                 */
//                def jarName = jarInput.name
//                def md5Name = DigestUtils.md5Hex(jarInput.file.getAbsolutePath())
//                if (jarName.endsWith(".jar")) {
//                    jarName = jarName.substring(0, jarName.length() - 4)
//                }
//
//                File tmpFile = null;
//                if (jarInput.file.getAbsolutePath().endsWith(".jar")) {
//                    JarFile jarFile = new JarFile(jarInput.file);
//                    Enumeration enumeration = jarFile.entries();
//                    tmpFile = new File(jarInput.file.getParent() + File.separator + "classes_trace.jar");
//                    //避免上次的缓存被重复插入
//                    if (tmpFile.exists()) {
//                        tmpFile.delete();
//                    }
//                    JarOutputStream jarOutputStream = new JarOutputStream(new FileOutputStream(tmpFile));
//                    //用于保存
//                    ArrayList<String> processorList = new ArrayList<>();
//                    while (enumeration.hasMoreElements()) {
//                        JarEntry jarEntry = (JarEntry) enumeration.nextElement();
//                        String entryName = jarEntry.getName();
//                        ZipEntry zipEntry = new ZipEntry(entryName);
//                        //println "MeetyouCost entryName :" + entryName
//                        InputStream inputStream = jarFile.getInputStream(jarEntry);
//                        //如果是inject文件就跳过
//
//                        //重点:插桩class
//                        if (entryName.endsWith(".class") && !entryName.contains("R\$") &&
//                                !entryName.contains("R.class") && !entryName.contains("BuildConfig.class")) {
//                            //class文件处理
//                            jarOutputStream.putNextEntry(zipEntry);
//                            ClassReader classReader = new ClassReader(IOUtils.toByteArray(inputStream))
//                            ClassWriter classWriter = new ClassWriter(classReader, ClassWriter.COMPUTE_MAXS)
//                            def className = entryName.split(".class")[0]
//                            ClassVisitor cv = new TraceVisitor(className, classWriter)
//                            classReader.accept(cv, EXPAND_FRAMES)
//                            byte[] code = classWriter.toByteArray()
//                            jarOutputStream.write(code)
//
//                        } else if (entryName.contains("META-INF/services/javax.annotation.processing.Processor")) {
//                            if (!processorList.contains(entryName)) {
//                                processorList.add(entryName)
//                                jarOutputStream.putNextEntry(zipEntry);
//                                jarOutputStream.write(IOUtils.toByteArray(inputStream));
//                            } else {
//                                println "duplicate entry:" + entryName
//                            }
//                        } else {
//
//                            jarOutputStream.putNextEntry(zipEntry);
//                            jarOutputStream.write(IOUtils.toByteArray(inputStream));
//                        }
//
//                        jarOutputStream.closeEntry();
//                    }
//                    //写入inject注解
//
//                    //结束
//                    jarOutputStream.close();
//                    jarFile.close();
//                }
//
//                //处理jar进行字节码注入处理 TODO
//
//                def dest = outputProvider.getContentLocation(jarName + md5Name,
//                        jarInput.contentTypes, jarInput.scopes, Format.JAR)
//                if (tmpFile == null) {
//                    FileUtils.copyFile(jarInput.file, dest)
//                } else {
//                    FileUtils.copyFile(tmpFile, dest)
//                    tmpFile.delete()
//                }
//            }
        }
        println("//============asm transform end===============//")
    }


}
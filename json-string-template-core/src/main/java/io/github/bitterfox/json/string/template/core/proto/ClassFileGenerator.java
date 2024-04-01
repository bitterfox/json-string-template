package io.github.bitterfox.json.string.template.core.proto;

import static java.lang.constant.ConstantDescs.CD_CallSite;
import static java.lang.constant.ConstantDescs.CD_MethodHandles_Lookup;
import static java.lang.constant.ConstantDescs.CD_MethodType;
import static java.lang.constant.ConstantDescs.CD_Object;
import static java.lang.constant.ConstantDescs.CD_String;
import static java.lang.constant.ConstantDescs.CD_void;

import java.io.IOException;
import java.lang.classfile.ClassFile;
import java.lang.classfile.constantpool.ConstantPoolBuilder;
import java.lang.constant.ClassDesc;
import java.lang.constant.DirectMethodHandleDesc;
import java.lang.constant.DirectMethodHandleDesc.Kind;
import java.lang.constant.DynamicCallSiteDesc;
import java.lang.constant.MethodHandleDesc;
import java.lang.constant.MethodTypeDesc;
import java.lang.reflect.Modifier;
import java.nio.file.Path;

public class ClassFileGenerator {
    public static class Main {
//        public static void doStringTemplate() throws Throwable {
//            var callsite = StringTemplateBSM.createStringTemplateRuntimeCallSite(MethodHandles.lookup(), "ignore", null);
//            String name = "Duke";
//            System.out.println(
//                    callsite.getTarget().invokeExact(
//                            StringTemplateSTRDebug.STR,
//                            new String[] { "Hello ", "" }, new Object[] { name })
//            );
//        }

        public static void main(String[] args) throws Throwable {
//            doStringTemplate();
//            doStringTemplate();
//            doStringTemplate();
        }
    }

    public static void main(String[] args) throws IOException {
        String package__ = ClassFileGenerator.class.getPackageName();
//        String package__ = "";
        String package_ = package__.isEmpty() ? "" : package__ + ".";

        ClassDesc clazz = ClassDesc.of("Main");

        ClassDesc StringTemplateBSM = ClassDesc.of(package_ + "StringTemplateBSM");
        DirectMethodHandleDesc createStringTemplateRuntimeCallSite =
                MethodHandleDesc.ofMethod(Kind.STATIC, StringTemplateBSM,
                                          "createStringTemplateRuntimeCallSite",
                                          MethodTypeDesc.of(CD_CallSite, CD_MethodHandles_Lookup, CD_String,
                                                            CD_MethodType));
        ClassDesc StringTemplateProcessorFactory = ClassDesc.of(
                package_ + "StringTemplateProcessorFactory");

        ClassFile.of()
                 .buildTo(Path.of("/tmp/Main.class"), clazz, classBuilder -> {
                     ConstantPoolBuilder constpool = classBuilder.constantPool();
                     classBuilder.withMethod(
                             "doStringTemplate", MethodTypeDesc.of(CD_void), Modifier.PUBLIC | Modifier.STATIC,
                             method -> {
                                 method.withCode(
                                         code -> {
                                             code.getstatic(ClassDesc.of(package_ + "StringTemplateSTRDebug"),
                                                            "STR", ClassDesc.of(package_ + "StringTemplateProcessorFactory"))
                                                 .iconst_2()
                                                 .anewarray(CD_String)
                                                 .dup()
                                                 .iconst_0()
                                                 .ldc(constpool.stringEntry("Hello "))
                                                 .aastore()
                                                 .dup()
                                                 .iconst_1()
                                                 .ldc(constpool.stringEntry(""))
                                                 .aastore()
                                                 .iconst_1()
                                                 .anewarray(CD_Object)
                                                 .dup()
                                                 .iconst_0()
                                                 .ldc(constpool.stringEntry("duke"))
                                                 .aastore()
                                                 .invokedynamic(
                                                         DynamicCallSiteDesc.of(
                                                                 createStringTemplateRuntimeCallSite,
                                                                 "process",
                                                                 MethodTypeDesc.of(CD_Object,
                                                                                   StringTemplateProcessorFactory,
                                                                                   CD_String.arrayType(),
                                                                                   CD_Object.arrayType())))
                                                 .astore(1)
                                                 .getstatic(ClassDesc.of("java.lang.System"), "out",
                                                            ClassDesc.of("java.io.PrintStream"))
                                                 .aload(1)
                                                 .invokevirtual(ClassDesc.of("java.io.PrintStream"), "println",
                                                                MethodTypeDesc.of(CD_void, CD_Object))
                                                 .return_();
                                         });
                             });

                     classBuilder.withMethod(
                             "main", MethodTypeDesc.of(CD_void, CD_String.arrayType()), Modifier.PUBLIC | Modifier.STATIC,
                             method -> {
                                 method.withCode(
                                         code -> {
                                             code.invokestatic(clazz, "doStringTemplate",
                                                               MethodTypeDesc.of(CD_void))
                                                 .invokestatic(clazz, "doStringTemplate",
                                                               MethodTypeDesc.of(CD_void))
                                                 .invokestatic(clazz, "doStringTemplate",
                                                               MethodTypeDesc.of(CD_void))
                                                 .return_();
                                         });
                             });
                 });
    }
}

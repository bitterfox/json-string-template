package io.github.bitterfox.json.string.template.core;

import java.lang.classfile.ClassBuilder;
import java.lang.classfile.CodeBuilder;
import java.lang.constant.ClassDesc;
import java.lang.reflect.Modifier;
import java.util.concurrent.atomic.AtomicInteger;

public class JsonCompilerV2Context {
    final ClassDesc clazz;
    final ClassBuilder classBuilder;
    final CodeBuilder classInitCode;
    final CodeBuilder processCode;

    private final AtomicInteger fieldNumber = new AtomicInteger();

    public JsonCompilerV2Context(ClassDesc clazz, ClassBuilder classBuilder, CodeBuilder classInitCode,
                                 CodeBuilder processCode) {
        this.clazz = clazz;
        this.classBuilder = classBuilder;
        this.classInitCode = classInitCode;
        this.processCode = processCode;
    }

    public String newField(ClassDesc type) {
        int i = fieldNumber.getAndIncrement();

        String name = "json" + i;
        classBuilder.withField(name, type, Modifier.PRIVATE | Modifier.STATIC);
        return name;
    }
}

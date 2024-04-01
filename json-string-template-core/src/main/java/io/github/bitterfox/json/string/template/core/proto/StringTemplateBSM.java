package io.github.bitterfox.json.string.template.core.proto;

import java.lang.invoke.CallSite;
import java.lang.invoke.ConstantCallSite;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.invoke.MethodType;

public class StringTemplateBSM {
    public static CallSite createStringTemplateRuntimeCallSite(Lookup lookup, String dynMethodName, MethodType dynMethodType) throws Throwable {
        StringTemplateRuntime runtime = new StringTemplateRuntime();
        MethodHandle target = lookup.bind(runtime, "process", MethodType.methodType(Object.class, StringTemplateProcessorFactory.class, String[].class, Object[].class));
        return new ConstantCallSite(target);
    }
}

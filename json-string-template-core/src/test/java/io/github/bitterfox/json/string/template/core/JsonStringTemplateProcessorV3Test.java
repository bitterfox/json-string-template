package io.github.bitterfox.json.string.template.core;

import static java.lang.constant.ConstantDescs.CD_Boolean;
import static java.lang.constant.ConstantDescs.CD_Collection;
import static java.lang.constant.ConstantDescs.CD_List;
import static java.lang.constant.ConstantDescs.CD_Map;
import static java.lang.constant.ConstantDescs.CD_Object;
import static java.lang.constant.ConstantDescs.CD_String;
import static java.lang.constant.ConstantDescs.CD_boolean;
import static java.lang.constant.ConstantDescs.CD_int;
import static java.lang.constant.ConstantDescs.CD_void;

import java.lang.classfile.CodeBuilder;
import java.lang.constant.ClassDesc;
import java.lang.constant.ConstantDescs;
import java.lang.constant.MethodTypeDesc;

public class JsonStringTemplateProcessorV3Test extends AbstractJsonStringTemplateProcessorTest {

    static class JavaObjectJsonCompilerBridge implements JsonCompilerBridge<Object> {

        @Override
        public ClassDesc jsonClass() {
            return CD_Object;
        }

        @Override
        public ClassDesc jsonObjectIntermediateClass() {
            return CD_Map;
        }

        @Override
        public ClassDesc jsonArrayIntermediateClass() {
            return CD_List;
        }

        @Override
        public void compileCreateJsonObjectIntermediate(JsonCompilerV2Context context, CodeBuilder code,
                                                        int size, String field) {
            code.new_(ClassDesc.of("java.util.HashMap"))
                .dup()
                .ldc(context.classBuilder.constantPool().intEntry(size))
                .invokespecial(ClassDesc.of("java.util.HashMap"), ConstantDescs.INIT_NAME, MethodTypeDesc.of(CD_void, CD_int));

            if (field != null) {
                code.dup()
                    .getstatic(context.clazz, field, jsonObjectIntermediateClass())
                    .invokeinterface(CD_Map, "putAll", MethodTypeDesc.of(CD_void, CD_Map));
            }
        }

        @Override
        public void compilePutToJsonObjectIntermediate(JsonCompilerV2Context context, CodeBuilder code) {
            code.invokeinterface(CD_Map, "put", MethodTypeDesc.of(CD_Object, CD_Object, CD_Object))
                .pop();
        }

        @Override
        public void compileFinishJsonObjectIntermediate(JsonCompilerV2Context context, CodeBuilder code) {
            // nop
        }

        @Override
        public boolean cacheJsonObjectIntermediate() {
            return true;
        }

        @Override
        public boolean isJsonObjectImmutable() {
            return false;
        }

        @Override
        public void compileCreateJsonArrayIntermediate(JsonCompilerV2Context context, CodeBuilder code,
                                                       int size, String field) {
            code.new_(ClassDesc.of("java.util.ArrayList"))
                .dup()
                .ldc(context.classBuilder.constantPool().intEntry(size))
                .invokespecial(ClassDesc.of("java.util.ArrayList"), ConstantDescs.INIT_NAME, MethodTypeDesc.of(CD_void, CD_int));

            if (field != null) {
                code.dup()
                    .getstatic(context.clazz, field, jsonArrayIntermediateClass())
                    .invokeinterface(CD_List, "addAll", MethodTypeDesc.of(CD_boolean, CD_Collection))
                        .pop();
            }
        }

        @Override
        public void compilePutToJsonArrayIntermediate(JsonCompilerV2Context context, CodeBuilder code) {
            code.invokeinterface(CD_List, "add", MethodTypeDesc.of(CD_boolean, CD_Object))
                .pop();
        }

        @Override
        public void compileFinishJsonArrayIntermediate(JsonCompilerV2Context context, CodeBuilder code) {
            // nop
        }

        @Override
        public boolean cacheJsonArrayIntermediate() {
            return true;
        }

        @Override
        public boolean isJsonArrayImmutable() {
            return false;
        }

        @Override
        public void compileString(JsonCompilerV2Context context, CodeBuilder code) {
            // nop
        }

        @Override
        public boolean isStringImmutable() {
            return true;
        }

        @Override
        public boolean cacheString() {
            return false;
        }

        @Override
        public void compileNumber(JsonCompilerV2Context context, CodeBuilder code, String number) {
            code.new_(ClassDesc.of("java.math.BigDecimal"))
                .dup()
                .ldc(context.classBuilder.constantPool().stringEntry(number))
                .invokespecial(ClassDesc.of("java.math.BigDecimal"), ConstantDescs.INIT_NAME, MethodTypeDesc.of(CD_void, CD_String));
        }

        @Override
        public void compileNumber(JsonCompilerV2Context context, CodeBuilder code, Number number) {
            // nop
        }

        @Override
        public boolean isNumberImmutable() {
            return true;
        }

        @Override
        public void compileTrue(JsonCompilerV2Context context, CodeBuilder code) {
            code.getstatic(CD_Boolean, "TRUE", CD_Boolean);
        }

        @Override
        public boolean isTrueImmutable() {
            return true;
        }

        @Override
        public void compileFalse(JsonCompilerV2Context context, CodeBuilder code) {
            code.getstatic(CD_Boolean, "FALSE", CD_Boolean);
        }

        @Override
        public boolean isFalseImmutable() {
            return true;
        }

        @Override
        public void compileNull(JsonCompilerV2Context context, CodeBuilder code) {
            code.aconst_null();
        }

        @Override
        public boolean isNullImmutable() {
            return true;
        }

        @Override
        public void compileJavaObject(JsonCompilerV2Context context, CodeBuilder processCode, int argPos) {
            processCode.aload(processCode.parameterSlot(argPos));
        }
    }

    @SuppressWarnings("removal")
    public JsonStringTemplateProcessorV3Test() {
        super(JsonStringTemplateProcessor.ofV3(new JavaObjectJsonCompilerBridge()));
    }
}
